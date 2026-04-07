package com.flash.film.module.log.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Bảng access log — ghi lại mọi request vào hệ thống.
 * Sensitive fields (password, secret...) được mask trước khi lưu.
 * Không extend BaseEntity vì đây là append-only log table.
 */
@Getter
@Setter
@Entity
@Table(name = "access_log", indexes = {
        @Index(name = "idx_access_log_user_id", columnList = "user_id"),
        @Index(name = "idx_access_log_request_at", columnList = "request_at")
})
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uri", length = 500)
    private String uri;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "params", columnDefinition = "TEXT")
    private String params;

    @Column(name = "from_ip", length = 50)
    private String fromIp;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_type", length = 20)
    private String userType;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "exception", columnDefinition = "TEXT")
    private String exception;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "request_at")
    private Timestamp requestAt;
}
