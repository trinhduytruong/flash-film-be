package com.flash.film.module.category.service.impl;

import com.flash.film.module.category.entity.Category;
import com.flash.film.module.category.dto.response.CategoryResponse;
import com.flash.film.module.category.repository.CategoryRepository;
import com.flash.film.module.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        // Fetch all active categories sorted by sortOrder
        List<Category> allCategories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"))
                .stream()
                .filter(Category::getIsActive)
                .collect(Collectors.toList());

        // Map them to DTOs
        Map<Long, CategoryResponse> dtoMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, this::mapToResponse));

        // Build the tree
        List<CategoryResponse> roots = new java.util.ArrayList<>();
        for (Category category : allCategories) {
            CategoryResponse response = dtoMap.get(category.getId());
            if (category.getParentId() == null) {
                roots.add(response);
            } else {
                CategoryResponse parent = dtoMap.get(category.getParentId());
                if (parent != null) {
                    parent.getSubCategories().add(response);
                }
            }
        }

        return roots;
    }

    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());
        response.setDescription(category.getDescription());
        response.setCategoryType(category.getCategoryType());
        response.setSeoTitle(category.getSeoTitle());
        response.setSeoDescription(category.getSeoDescription());
        return response;
    }
}
