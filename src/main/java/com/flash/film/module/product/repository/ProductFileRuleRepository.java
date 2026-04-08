package com.flash.film.module.product.repository;

import com.flash.film.module.product.entity.ProductFileRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductFileRuleRepository extends JpaRepository<ProductFileRule, Long> {
    List<ProductFileRule> findByProductIdAndIsActiveTrue(Long productId);
}
