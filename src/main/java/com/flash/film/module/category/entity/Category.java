package com.flash.film.module.category.entity;

import com.flash.film.common.entity.BaseEntity;
import com.flash.film.common.enums.CategoryType;
import com.flash.film.common.enums.SourceSystem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_category_type", columnList = "category_type"),
        @Index(name = "idx_category_source", columnList = "source_system")
})
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", length = 30, nullable = false)
    private CategoryType categoryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_system", length = 30, nullable = false)
    private SourceSystem sourceSystem = SourceSystem.FLASH_DTF;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "slug", length = 180, nullable = false, unique = true)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "seo_title", length = 255)
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Category parentCategory;
}
