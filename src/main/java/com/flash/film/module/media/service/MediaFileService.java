package com.flash.film.module.media.service;

import com.flash.film.common.dto.ApiResponse;
import com.flash.film.module.media.dto.MediaFileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MediaFileService {
    ApiResponse<MediaFileResponse> uploadArtwork(MultipartFile file, Long userId);
}
