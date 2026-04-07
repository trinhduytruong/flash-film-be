package com.flash.film.module.user.entity;

import com.flash.film.common.entity.BaseEntity;
import com.flash.film.common.enums.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;


/**
 * Bảng users — lưu thông tin người dùng.
 * jwtSecret là secret riêng mỗi user để ký JWT (rotate = invalidate toàn bộ
 * token).
 * userType xác định nhóm quyền (ADMIN, MODERATOR, USER).
 */
@Getter
@Setter
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true),
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType = UserType.USER;

    @Column(name = "last_login")
    private Timestamp lastLogin;

    @Column(name = "jwt_secret", nullable = false)
    private String jwtSecret;
}
