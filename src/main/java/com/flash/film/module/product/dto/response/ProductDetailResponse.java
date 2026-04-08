package com.flash.film.module.product.dto.response;

import com.flash.film.common.enums.ProductType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProductDetailResponse {
    private Long id;
    private Long categoryId;
    private String sku;
    private String name;
    private String slug;
    private String shortDescription;
    private String descriptionHtml;
    private ProductType productType;
    private Boolean isCustomUpload;
    private Integer leadTimeDays;
    
    // Nested data
    private List<ProductVariantResponse> variants = new ArrayList<>();
    private List<ProductPriceRuleResponse> priceRules = new ArrayList<>();
    private List<ProductFileRuleResponse> fileRules = new ArrayList<>();
    private List<ProductMediaResponse> media = new ArrayList<>();
}
