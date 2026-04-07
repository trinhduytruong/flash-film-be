package com.flash.film.module.log.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class AccessLoggerService {

    @Value("${app.logging.access-log-file:logs/access.log}")
    private String logFilePath;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final ExecutorService fileWriterExecutor = Executors.newSingleThreadExecutor();

    public void logAccessAsync(Map<String, Object> logData) {
        fileWriterExecutor.submit(() -> {
            try {
                Path path = Paths.get(logFilePath);
                if (!Files.exists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
                
                String jsonLogLine = objectMapper.writeValueAsString(logData) + System.lineSeparator();
                log.info("Saving log to file: {}", logFilePath);
                Files.write(path, jsonLogLine.getBytes(StandardCharsets.UTF_8), 
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {
                log.error("Unable to write access logs to a file at the specified path [{}]: {}", logFilePath, e.getMessage(), e);
            }
        });
    }
}
