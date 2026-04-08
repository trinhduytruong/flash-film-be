package com.flash.film.module.product.repository;

import com.flash.film.module.product.entity.ProductPriceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductPriceRuleRepository extends JpaRepository<ProductPriceRule, Long> {
    List<ProductPriceRule> findByProductIdAndIsActiveTrue(Long productId);
}
