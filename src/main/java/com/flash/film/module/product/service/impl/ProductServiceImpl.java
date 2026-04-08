package com.flash.film.module.product.service.impl;

import com.flash.film.common.enums.AppCode;
import com.flash.film.common.exception.CustomException;
import com.flash.film.module.product.entity.*;
import com.flash.film.module.product.dto.response.*;
import com.flash.film.module.product.repository.*;
import com.flash.film.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductPriceRuleRepository priceRuleRepository;
    private final ProductFileRuleRepository fileRuleRepository;
    private final ProductMediaRepository mediaRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponse> getPublishedProducts(Long categoryId, String keyword, Pageable pageable) {
        Page<Product> products = productRepository.findPublishedProducts(categoryId, keyword, pageable);
        return products.map(product -> {
            ProductListResponse dto = new ProductListResponse();
            dto.setId(product.getId());
            dto.setSku(product.getSku());
            dto.setName(product.getName());
            dto.setSlug(product.getSlug());
            dto.setShortDescription(product.getShortDescription());
            dto.setProductType(product.getProductType());
            
            // Lấy 1 giá nhỏ nhất
            List<ProductPriceRule> rules = priceRuleRepository.findByProductIdAndIsActiveTrue(product.getId());
            BigDecimal minPrice = rules.stream().map(ProductPriceRule::getUnitPrice).min(BigDecimal::compareTo).orElse(null);
            dto.setMinPrice(minPrice);
            
            // Lấy ảnh bìa
            List<ProductMedia> medias = mediaRepository.findWithMediaFileByProductId(product.getId());
            dto.setThumbnailPath(medias.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsPrimary()) || "THUMBNAIL".equals(m.getMediaRole().name()))
                .findFirst()
                .map(m -> m.getMediaFile() != null ? m.getMediaFile().getStoragePath() : null)
                .orElse(null));
                
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetailBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new CustomException(AppCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Product not found"));

        if (!product.getIsActive() || !"ACTIVE".equals(product.getStatus().name())) {
            throw new CustomException(AppCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Product is not available");
        }

        ProductDetailResponse res = new ProductDetailResponse();
        res.setId(product.getId());
        res.setCategoryId(product.getCategoryId());
        res.setSku(product.getSku());
        res.setName(product.getName());
        res.setSlug(product.getSlug());
        res.setShortDescription(product.getShortDescription());
        res.setDescriptionHtml(product.getDescriptionHtml());
        res.setProductType(product.getProductType());
        res.setIsCustomUpload(product.getIsCustomUpload());
        res.setLeadTimeDays(product.getLeadTimeDays());

        // Load Variants
        List<ProductVariant> variants = variantRepository.findByProductIdAndIsActiveTrueOrderBySortOrderAsc(product.getId());
        res.setVariants(variants.stream().map(v -> {
            ProductVariantResponse vr = new ProductVariantResponse();
            vr.setId(v.getId());
            vr.setSku(v.getSku());
            vr.setOptions(v.getOptionSnapshotJson());
            vr.setSizeLabel(v.getSizeLabel());
            vr.setColorLabel(v.getColorLabel());
            vr.setSortOrder(v.getSortOrder());
            vr.setStatus(v.getStatus().name());
            return vr;
        }).collect(Collectors.toList()));

        // Load Price Rules
        List<ProductPriceRule> priceRules = priceRuleRepository.findByProductIdAndIsActiveTrue(product.getId());
        res.setPriceRules(priceRules.stream().map(pr -> {
            ProductPriceRuleResponse prr = new ProductPriceRuleResponse();
            prr.setId(pr.getId());
            prr.setVariantId(pr.getProductVariantId());
            prr.setPriceType(pr.getPriceType());
            prr.setMinQty(pr.getMinQty());
            prr.setMaxQty(pr.getMaxQty());
            prr.setWidthInch(pr.getWidthInch());
            prr.setHeightInch(pr.getHeightInch());
            prr.setSheetLengthInch(pr.getSheetLengthInch());
            prr.setUnitPrice(pr.getUnitPrice());
            prr.setExtraCharge(pr.getExtraCharge());
            return prr;
        }).collect(Collectors.toList()));

        // Load File Rules
        List<ProductFileRule> fileRules = fileRuleRepository.findByProductIdAndIsActiveTrue(product.getId());
        res.setFileRules(fileRules.stream().map(fr -> {
            ProductFileRuleResponse frr = new ProductFileRuleResponse();
            frr.setId(fr.getId());
            frr.setAllowedExtensions(fr.getAllowedExtensions());
            frr.setMinDpi(fr.getMinDpi());
            frr.setMinWidthPx(fr.getMinWidthPx());
            frr.setMinHeightPx(fr.getMinHeightPx());
            frr.setMaxFileMb(fr.getMaxFileMb());
            frr.setTransparentRequired(fr.getTransparentRequired());
            frr.setNotes(fr.getNotes());
            return frr;
        }).collect(Collectors.toList()));

        // Load Media
        List<ProductMedia> medias = mediaRepository.findWithMediaFileByProductId(product.getId());
        res.setMedia(medias.stream().map(m -> {
            ProductMediaResponse mr = new ProductMediaResponse();
            mr.setId(m.getId());
            mr.setMediaRole(m.getMediaRole());
            mr.setSortOrder(m.getSortOrder());
            mr.setIsPrimary(m.getIsPrimary());
            if (m.getMediaFile() != null) {
                mr.setStoragePath(m.getMediaFile().getStoragePath());
                mr.setMimeType(m.getMediaFile().getMimeType());
                mr.setFileSizeBytes(m.getMediaFile().getFileSizeBytes());
                mr.setWidthPx(m.getMediaFile().getWidthPx());
                mr.setHeightPx(m.getMediaFile().getHeightPx());
                mr.setDpi(m.getMediaFile().getDpi());
            }
            return mr;
        }).collect(Collectors.toList()));

        return res;
    }
}
