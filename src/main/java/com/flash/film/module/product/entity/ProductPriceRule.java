package com.flash.film.module.product.entity;

import com.flash.film.common.entity.BaseEntity;
import com.flash.film.common.enums.PriceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "product_price_rules", indexes = {
        @Index(name = "idx_price_rule_type", columnList = "price_type")
})
public class ProductPriceRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_variant_id")
    private Long productVariantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", length = 30, nullable = false)
    private PriceType priceType;

    @Column(name = "min_qty")
    private Integer minQty;

    @Column(name = "max_qty")
    private Integer maxQty;

    @Column(name = "width_inch", precision = 8, scale = 2)
    private BigDecimal widthInch;

    @Column(name = "height_inch", precision = 8, scale = 2)
    private BigDecimal heightInch;

    @Column(name = "sheet_length_inch", precision = 8, scale = 2)
    private BigDecimal sheetLengthInch;

    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "extra_charge", precision = 12, scale = 2, nullable = false)
    private BigDecimal extraCharge = BigDecimal.ZERO;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", insertable = false, updatable = false)
    private ProductVariant variant;
}
