package com.flash.film.module.product.controller;

import com.flash.film.common.dto.ApiResponse;
import com.flash.film.common.enums.AppCode;
import com.flash.film.module.product.dto.response.ProductDetailResponse;
import com.flash.film.module.product.dto.response.ProductListResponse;
import com.flash.film.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/film/public/v1/products")
@RequiredArgsConstructor
public class ProductPublicController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductListResponse> page = productService.getPublishedProducts(categoryId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page, AppCode.SUCCESS));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@PathVariable String slug) {
        ProductDetailResponse detail = productService.getProductDetailBySlug(slug);
        return ResponseEntity.ok(ApiResponse.ok(detail, AppCode.SUCCESS));
    }
}
