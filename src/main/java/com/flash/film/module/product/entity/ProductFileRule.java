package com.flash.film.module.product.entity;

import com.flash.film.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "product_file_rules")
public class ProductFileRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "allowed_extensions", length = 255, nullable = false)
    private String allowedExtensions;

    @Column(name = "min_dpi")
    private Integer minDpi;

    @Column(name = "min_width_px")
    private Integer minWidthPx;

    @Column(name = "min_height_px")
    private Integer minHeightPx;

    @Column(name = "max_file_mb", precision = 8, scale = 2)
    private BigDecimal maxFileMb;

    @Column(name = "transparent_required", nullable = false)
    private Boolean transparentRequired = false;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
}
