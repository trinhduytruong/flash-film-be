package com.flash.film.module.media.service.impl;

import com.flash.film.common.enums.StorageDisk;
import com.flash.film.module.media.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.use-s3", havingValue = "true")
public class S3StorageServiceImpl implements StorageService {

    @Value("${app.storage.s3.region}")
    private String region;

    @Value("${app.storage.s3.bucket}")
    private String bucket;

    @Value("${app.storage.s3.access-key}")
    private String accessKey;

    @Value("${app.storage.s3.secret-key}")
    private String secretKey;

    @Value("${app.storage.s3.base-url}")
    private String baseUrl;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
        log.info("Initialized S3 Storage Provider for bucket: {}", bucket);
    }

    @Override
    public String uploadFile(MultipartFile file, String folderName) throws IOException {
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String uniqueFileName = UUID.randomUUID().toString() + extension;
        String s3Key = (folderName != null && !folderName.isEmpty() ? folderName + "/" : "") + uniqueFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
        log.info("File uploaded to S3: s3://{}/{}", bucket, s3Key);

        return s3Key;
    }

    @Override
    public void deleteFile(String storagePath) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(storagePath)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("Deleted file from S3: {}", storagePath);
        } catch (Exception e) {
            log.error("Failed to delete S3 file: {}", storagePath, e);
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
        return StorageDisk.S3;
    }
}
