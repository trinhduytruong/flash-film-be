package com.flash.film.module.product.dto.response;

import com.flash.film.common.enums.MediaRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductMediaResponse {
    private Long id;
    private String storagePath;
    private String mimeType;
    private Long fileSizeBytes;
    private Integer widthPx;
    private Integer heightPx;
    private Integer dpi;
    private MediaRole mediaRole;
    private Integer sortOrder;
    private Boolean isPrimary;
}
