package com.flash.film.module.category.dto.response;

import com.flash.film.common.enums.CategoryType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private CategoryType categoryType;
    private String seoTitle;
    private String seoDescription;
    private List<CategoryResponse> subCategories = new ArrayList<>();
}
