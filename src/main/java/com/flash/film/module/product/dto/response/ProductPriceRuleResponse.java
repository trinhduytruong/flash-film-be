package com.flash.film.module.product.dto.response;

import com.flash.film.common.enums.PriceType;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductPriceRuleResponse {
    private Long id;
    private Long variantId;
    private PriceType priceType;
    private Integer minQty;
    private Integer maxQty;
    private BigDecimal widthInch;
    private BigDecimal heightInch;
    private BigDecimal sheetLengthInch;
    private BigDecimal unitPrice;
    private BigDecimal extraCharge;
}
