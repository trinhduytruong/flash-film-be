package com.flash.film.module.product.service;

import com.flash.film.module.product.dto.response.ProductDetailResponse;
import com.flash.film.module.product.dto.response.ProductListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductListResponse> getPublishedProducts(Long categoryId, String keyword, Pageable pageable);
    ProductDetailResponse getProductDetailBySlug(String slug);
}
