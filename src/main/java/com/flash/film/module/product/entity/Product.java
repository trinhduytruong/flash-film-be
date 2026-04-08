package com.flash.film.module.product.entity;

import com.flash.film.common.entity.BaseEntity;
import com.flash.film.common.enums.ProductStatus;
import com.flash.film.common.enums.ProductType;
import com.flash.film.common.enums.SourceSystem;
import com.flash.film.module.category.entity.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_source", columnList = "source_system"),
        @Index(name = "idx_product_type", columnList = "product_type"),
        @Index(name = "idx_product_status", columnList = "status")
})
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_system", length = 30, nullable = false)
    private SourceSystem sourceSystem = SourceSystem.FLASH_DTF;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", length = 40, nullable = false)
    private ProductType productType;

    @Column(name = "sku", length = 80, nullable = false, unique = true)
    private String sku;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "slug", length = 220, nullable = false, unique = true)
    private String slug;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "description_html", columnDefinition = "LONGTEXT")
    private String descriptionHtml;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(name = "is_custom_upload", nullable = false)
    private Boolean isCustomUpload = false;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Column(name = "seo_title", length = 255)
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;
}
