-- ============================================================
-- Film BE — Database Initialization Script
-- MySQL 8.0 | Character set: utf8mb4
-- ============================================================

USE film_db;

-- ──────────────────────────────────────────────────────────────
-- 1. users
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(200) DEFAULT NULL,
    user_type   VARCHAR(20)  NOT NULL DEFAULT 'USER',
    last_login  TIMESTAMP    NULL DEFAULT NULL,
    jwt_secret  VARCHAR(255) NOT NULL,

    -- BaseEntity fields
    created_at  TIMESTAMP    NULL DEFAULT NULL,
    created_by  VARCHAR(255) DEFAULT NULL,
    updated_at  TIMESTAMP    NULL DEFAULT NULL,
    updated_by  VARCHAR(255) DEFAULT NULL,
    is_active   TINYINT(1)   NOT NULL DEFAULT 1,

    PRIMARY KEY (id),
    UNIQUE KEY idx_users_username (username),
    UNIQUE KEY idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────
-- 2. user_token
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_token (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    user_id            BIGINT       NOT NULL,
    access_token       TEXT         DEFAULT NULL,
    refresh_token      TEXT         DEFAULT NULL,
    device_ip          VARCHAR(50)  DEFAULT NULL,
    user_agent         VARCHAR(500) DEFAULT NULL,
    access_expires_at  TIMESTAMP    NULL DEFAULT NULL,
    refresh_expires_at TIMESTAMP    NULL DEFAULT NULL,
    is_revoked         TINYINT(1)   NOT NULL DEFAULT 0,
    revoked_at         TIMESTAMP    NULL DEFAULT NULL,
    revoke_reason      VARCHAR(50)  DEFAULT NULL,

    -- BaseEntity fields
    created_at         TIMESTAMP    NULL DEFAULT NULL,
    created_by         VARCHAR(255) DEFAULT NULL,
    updated_at         TIMESTAMP    NULL DEFAULT NULL,
    updated_by         VARCHAR(255) DEFAULT NULL,
    is_active          TINYINT(1)   NOT NULL DEFAULT 1,

    PRIMARY KEY (id),
    INDEX idx_user_token_user_id (user_id),
    INDEX idx_user_token_access (access_token(700)),
    INDEX idx_user_token_refresh (refresh_token(700))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────
-- 3. permission_api
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS permission_api (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_type   VARCHAR(20)  NOT NULL,
    http_method VARCHAR(10)  NOT NULL,
    uri_pattern VARCHAR(300) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,

    -- BaseEntity fields
    created_at  TIMESTAMP    NULL DEFAULT NULL,
    created_by  VARCHAR(255) DEFAULT NULL,
    updated_at  TIMESTAMP    NULL DEFAULT NULL,
    updated_by  VARCHAR(255) DEFAULT NULL,
    is_active   TINYINT(1)   NOT NULL DEFAULT 1,

    PRIMARY KEY (id),
    INDEX idx_perm_user_type (user_type),
    INDEX idx_perm_method_uri (http_method, uri_pattern)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────
-- 4. access_log
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS access_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    uri          VARCHAR(500) DEFAULT NULL,
    http_method  VARCHAR(10)  DEFAULT NULL,
    params       TEXT         DEFAULT NULL,
    from_ip      VARCHAR(50)  DEFAULT NULL,
    device_id    VARCHAR(100) DEFAULT NULL,
    user_id      BIGINT       DEFAULT NULL,
    user_type    VARCHAR(20)  DEFAULT NULL,
    request_body TEXT         DEFAULT NULL,
    http_status  INT          DEFAULT NULL,
    exception    TEXT         DEFAULT NULL,
    duration_ms  BIGINT       DEFAULT NULL,
    request_at   TIMESTAMP    NULL DEFAULT NULL,

    PRIMARY KEY (id),
    INDEX idx_access_log_user_id (user_id),
    INDEX idx_access_log_request_at (request_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────
-- 5. redis_connection_log
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS redis_connection_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    operation     VARCHAR(30)  DEFAULT NULL,
    redis_key     VARCHAR(300) DEFAULT NULL,
    status        VARCHAR(20)  DEFAULT NULL,
    error_message TEXT         DEFAULT NULL,
    executed_at   TIMESTAMP    NULL DEFAULT NULL,

    PRIMARY KEY (id),
    INDEX idx_redis_log_executed_at (executed_at),
    INDEX idx_redis_log_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────
-- 6. rabbitmq_connection_log
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rabbitmq_connection_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    exchange_name VARCHAR(200) DEFAULT NULL,
    queue_name    VARCHAR(200) DEFAULT NULL,
    routing_key   VARCHAR(200) DEFAULT NULL,
    message_id    VARCHAR(100) DEFAULT NULL,
    message_body  TEXT         DEFAULT NULL,
    status        VARCHAR(30)  DEFAULT NULL,
    error_message TEXT         DEFAULT NULL,
    executed_at   TIMESTAMP    NULL DEFAULT NULL,

    PRIMARY KEY (id),
    INDEX idx_rmq_log_executed_at (executed_at),
    INDEX idx_rmq_log_status (status),
    INDEX idx_rmq_log_queue (queue_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────
-- Seed: Default ADMIN permissions (full access)
-- ──────────────────────────────────────────────────────────────
INSERT INTO permission_api (user_type, http_method, uri_pattern, description, created_at, is_active)
VALUES
    ('ADMIN', 'GET',    '/film/**', 'Admin full GET access',    NOW(), 1),
    ('ADMIN', 'POST',   '/film/**', 'Admin full POST access',   NOW(), 1),
    ('ADMIN', 'PUT',    '/film/**', 'Admin full PUT access',    NOW(), 1),
    ('ADMIN', 'PATCH',  '/film/**', 'Admin full PATCH access',  NOW(), 1),
    ('ADMIN', 'DELETE', '/film/**', 'Admin full DELETE access',  NOW(), 1);
