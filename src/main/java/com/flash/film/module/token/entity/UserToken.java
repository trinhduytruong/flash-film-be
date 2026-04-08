package com.flash.film.module.token.entity;

import com.flash.film.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "user_token", indexes = {
        @Index(name = "idx_user_token_access", columnList = "access_token", unique = true),
        @Index(name = "idx_user_token_refresh", columnList = "refresh_token"),
        @Index(name = "idx_user_token_user_id", columnList = "user_id"),
        @Index(name = "idx_user_token_refresh_expires", columnList = "refresh_expires_at")
})
public class UserToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "access_token", columnDefinition = "TEXT", unique = true)
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "device_ip", length = 50)
    private String deviceIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "access_expires_at")
    private Timestamp accessExpiresAt;

    @Column(name = "refresh_expires_at")
    private Timestamp refreshExpiresAt;

    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = false;

    @Column(name = "revoked_at")
    private Timestamp revokedAt;

    /** LOGOUT / EXPIRED / ADMIN_REVOKE */
    @Column(name = "revoke_reason", length = 50)
    private String revokeReason;
}
