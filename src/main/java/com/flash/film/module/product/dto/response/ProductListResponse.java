package com.flash.film.module.product.dto.response;

import com.flash.film.common.enums.ProductType;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductListResponse {
    private Long id;
    private String sku;
    private String name;
    private String slug;
    private String shortDescription;
    private ProductType productType;
    private BigDecimal minPrice;
    private String thumbnailPath;
}
