package com.flash.film.module.product.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class ProductVariantResponse {
    private Long id;
    private String sku;
    private Map<String, Object> options;
    private String sizeLabel;
    private String colorLabel;
    private Integer sortOrder;
    private String status;
}
