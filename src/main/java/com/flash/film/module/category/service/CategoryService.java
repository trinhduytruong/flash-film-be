package com.flash.film.module.category.service;

import com.flash.film.module.category.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getCategoryTree();
}
