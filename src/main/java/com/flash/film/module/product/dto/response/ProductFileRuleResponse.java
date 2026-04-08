package com.flash.film.module.product.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductFileRuleResponse {
    private Long id;
    private String allowedExtensions;
    private Integer minDpi;
    private Integer minWidthPx;
    private Integer minHeightPx;
    private BigDecimal maxFileMb;
    private Boolean transparentRequired;
    private String notes;
}
