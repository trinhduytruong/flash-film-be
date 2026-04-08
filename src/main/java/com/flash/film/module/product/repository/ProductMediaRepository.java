package com.flash.film.module.product.repository;

import com.flash.film.module.product.entity.ProductMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductMediaRepository extends JpaRepository<ProductMedia, Long> {
    
    @Query("SELECT pm FROM ProductMedia pm JOIN FETCH pm.mediaFile WHERE pm.productId = :productId AND pm.isActive = true ORDER BY pm.sortOrder ASC")
    List<ProductMedia> findWithMediaFileByProductId(@Param("productId") Long productId);
    
}
