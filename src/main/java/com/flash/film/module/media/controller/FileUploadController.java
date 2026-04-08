package com.flash.film.module.media.controller;

import com.flash.film.common.dto.ApiResponse;
import com.flash.film.common.config.security.CustomUserDetails;
import com.flash.film.module.media.dto.MediaFileResponse;
import com.flash.film.module.media.service.MediaFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/film/user/v1/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final MediaFileService mediaFileService;

    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MediaFileResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(mediaFileService.uploadFile(file, type, userId));
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }
        return null;
    }
}
