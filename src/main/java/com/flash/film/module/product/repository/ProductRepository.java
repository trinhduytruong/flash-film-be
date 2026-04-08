package com.flash.film.module.product.repository;

import com.flash.film.module.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.status = 'ACTIVE' " +
            "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
            "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> findPublishedProducts(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            Pageable pageable);
}
