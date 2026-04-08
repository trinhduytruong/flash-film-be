package com.flash.film.module.category.controller;

import com.flash.film.common.dto.ApiResponse;
import com.flash.film.module.category.dto.response.CategoryResponse;
import com.flash.film.module.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.flash.film.common.enums.AppCode;

@RestController
@RequestMapping("/film/public/v1/categories")
@RequiredArgsConstructor
public class CategoryPublicController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryTree() {
        List<CategoryResponse> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.ok(tree, AppCode.SUCCESS));
    }
}
