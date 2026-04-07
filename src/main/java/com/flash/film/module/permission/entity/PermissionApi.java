package com.flash.film.module.permission.entity;

import com.flash.film.common.entity.BaseEntity;
import com.flash.film.common.enums.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Bảng phân quyền API — định nghĩa user_type nào được phép gọi endpoint nào.
 * uri_pattern hỗ trợ wildcard
 */
@Getter
@Setter
@Entity
@Table(name = "permission_api", indexes = {
        @Index(name = "idx_perm_user_type", columnList = "user_type"),
        @Index(name = "idx_perm_method_uri", columnList = "http_method, uri_pattern")
})
public class PermissionApi extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "uri_pattern", nullable = false, length = 300)
    private String uriPattern;

    @Column(name = "description", length = 500)
    private String description;
}
