package com.flash.film.module.media.dto;

import com.flash.film.common.enums.MediaType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MediaFileResponse {
    private Long id;
    private MediaType fileType;
    private String originalName;
    private String mimeType;
    private Long fileSizeBytes;
    private String url;
    private String status;
}
