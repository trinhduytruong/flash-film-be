package com.flash.film.module.log.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flash.film.module.log.service.RabbitmqLoggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
public class RabbitmqLoggerServiceImpl implements RabbitmqLoggerService {

    @Value("${app.logging.rabbitmq-log-file}")
    private String logFilePath;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final ExecutorService fileWriterExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void logRabbitmqAsync(Map<String, Object> logData) {
        fileWriterExecutor.submit(() -> {
            try {
                Path path = Paths.get(logFilePath);
                if (!Files.exists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
                
                String jsonLogLine = objectMapper.writeValueAsString(logData) + System.lineSeparator();
                Files.write(path, jsonLogLine.getBytes(StandardCharsets.UTF_8), 
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {
                log.error("Unable to write rabbitmq logs to file [{}]: {}", logFilePath, e.getMessage(), e);
            }
        });
    }
}
