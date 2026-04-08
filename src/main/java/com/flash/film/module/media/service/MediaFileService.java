package com.flash.film.module.media.service;

import com.flash.film.common.dto.ApiResponse;
import com.flash.film.module.media.dto.MediaFileResponse;
import org.springframework.web.multipart.MultipartFile;

import com.flash.film.common.enums.MediaType;

public interface MediaFileService {
    ApiResponse<MediaFileResponse> uploadFile(MultipartFile file, String type, Long userId);
}
