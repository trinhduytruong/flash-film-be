package com.flash.film.module.product.entity;

import com.flash.film.common.entity.BaseEntity;
import com.flash.film.common.enums.MediaRole;
import com.flash.film.module.media.entity.MediaFile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_media", indexes = {
        @Index(name = "idx_pm_role", columnList = "media_role")
})
public class ProductMedia extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "media_file_id", nullable = false)
    private Long mediaFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_role", length = 30, nullable = false)
    private MediaRole mediaRole;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_file_id", insertable = false, updatable = false)
    private MediaFile mediaFile;
}
