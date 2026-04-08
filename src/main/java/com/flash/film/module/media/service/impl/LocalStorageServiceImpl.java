package com.flash.film.module.media.service.impl;

import com.flash.film.common.enums.StorageDisk;
import com.flash.film.module.media.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.use-s3", havingValue = "false", matchIfMissing = true)
public class LocalStorageServiceImpl implements StorageService {

    @Value("${app.storage.local.upload-dir}")
    private String baseUploadDir;

    @Value("${app.storage.local.base-url}")
    private String baseUrl;

    @Override
    public String uploadFile(MultipartFile file, String folderName) throws IOException {
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        // Generate a unique filename: folder/uuid.ext
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        String relativeStoragePath = (folderName != null && !folderName.isEmpty() ? folderName + "/" : "") + uniqueFileName;
        
        Path targetPath = Paths.get(baseUploadDir, relativeStoragePath);
        
        // Ensure directories exist
        if (!Files.exists(targetPath.getParent())) {
            Files.createDirectories(targetPath.getParent());
        }

        // Save file
        Files.write(targetPath, file.getBytes());
        log.info("File saved to local storage: {}", targetPath);

        return relativeStoragePath;
    }

    @Override
    public void deleteFile(String storagePath) {
        Path targetPath = Paths.get(baseUploadDir, storagePath);
        try {
            Files.deleteIfExists(targetPath);
            log.info("Deleted file from local storage: {}", targetPath);
        } catch (IOException e) {
            log.error("Failed to delete local file: {}", storagePath, e);
        }
    }

    @Override
    public String getFileUrl(String storagePath) {
        if (!baseUrl.endsWith("/")) {
            return baseUrl + "/" + storagePath;
        }
        return baseUrl + storagePath;
    }

    @Override
    public StorageDisk getStorageDisk() {
        return StorageDisk.LOCAL;
    }
}
