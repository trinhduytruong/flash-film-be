package com.flash.film.module.media.service.impl;

import com.flash.film.common.dto.ApiResponse;
import com.flash.film.common.enums.AppCode;
import com.flash.film.common.enums.MediaType;
import com.flash.film.common.exception.CustomException;
import com.flash.film.module.media.dto.MediaFileResponse;
import com.flash.film.module.media.entity.MediaFile;
import com.flash.film.module.media.repository.MediaFileRepository;
import com.flash.film.module.media.service.MediaFileService;
import com.flash.film.module.media.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaFileServiceImpl implements MediaFileService {

    private final StorageService storageService;
    private final MediaFileRepository mediaFileRepository;

    @Value("${app.storage.allowed-mime-types}")
    private List<String> allowedMimeTypes;

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    @Override
    @Transactional
    public ApiResponse<MediaFileResponse> uploadArtwork(MultipartFile file, Long userId) {
        validateFile(file);

        try {
            // Upload file to the active storage provider
            String storagePath = storageService.uploadFile(file, "artworks");

            MediaFile mediaFile = new MediaFile();
            mediaFile.setUserId(userId);
            mediaFile.setFileType(MediaType.ARTWORK);
            mediaFile.setStorageDisk(storageService.getStorageDisk());
            mediaFile.setStoragePath(storagePath);
            mediaFile.setOriginalName(file.getOriginalFilename());
            mediaFile.setMimeType(file.getContentType());
            mediaFile.setFileSizeBytes(file.getSize());
            mediaFile.setStatus("ACTIVE");
            mediaFile.setCreatedBy(userId != null ? userId.toString() : "SYSTEM");
            mediaFile.setUpdatedBy(mediaFile.getCreatedBy());

            MediaFile savedMedia = mediaFileRepository.save(mediaFile);

            MediaFileResponse response = MediaFileResponse.builder()
                    .id(savedMedia.getId())
                    .fileType(savedMedia.getFileType())
                    .originalName(savedMedia.getOriginalName())
                    .mimeType(savedMedia.getMimeType())
                    .fileSizeBytes(savedMedia.getFileSizeBytes())
                    .url(storageService.getFileUrl(savedMedia.getStoragePath()))
                    .status(savedMedia.getStatus())
                    .build();

            return ApiResponse.ok(response, AppCode.SUCCESS);

        } catch (IOException e) {
            log.error("Failed to upload artwork: {}", e.getMessage(), e);
            throw new CustomException(AppCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(AppCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "File is empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(AppCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "File is too large. Max 100MB allowed");
        }

        String mimeType = file.getContentType();
        if (mimeType == null || !allowedMimeTypes.contains(mimeType)) {
            throw new CustomException(AppCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "File format is not allowed. Allowed types: " + allowedMimeTypes);
        }
    }
}
