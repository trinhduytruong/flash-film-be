-- ============================================================
-- Film BE — Database Initialization Script
-- MySQL 8.0 | Character set: utf8mb4
-- ============================================================

USE film_db;

-- ──────────────────────────────────────────────────────────────
-- 1. users
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính',
    username    VARCHAR(100) NOT NULL COMMENT 'Tài khoản đăng nhập',
    email       VARCHAR(150) NOT NULL COMMENT 'Địa chỉ Email',
    password    VARCHAR(255) NOT NULL COMMENT 'Mật khẩu',
    first_name  VARCHAR(100) DEFAULT NULL COMMENT 'Tên người dùng',
    last_name   VARCHAR(100) DEFAULT NULL COMMENT 'Họ người dùng',
    phone_number VARCHAR(20) DEFAULT NULL COMMENT 'Số điện thoại',
    gender      VARCHAR(20)  DEFAULT NULL COMMENT 'Giới tính (MALE, FEMALE, OTHER)',
    date_of_birth DATE       DEFAULT NULL COMMENT 'Ngày sinh',
    company     VARCHAR(200) DEFAULT NULL COMMENT 'Tên công ty (Tùy chọn)',
    user_type   VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT 'Phân quyền Role',
    last_login  TIMESTAMP    NULL DEFAULT NULL COMMENT 'Lần cuối đăng nhập',
    jwt_secret  VARCHAR(255) NOT NULL COMMENT 'Mã bí mật chữ ký JWT',

    -- BaseEntity fields
    created_at  TIMESTAMP    NULL DEFAULT NULL COMMENT 'Ngày tạo',
    created_by  VARCHAR(255) DEFAULT NULL COMMENT 'Người tạo',
    updated_at  TIMESTAMP    NULL DEFAULT NULL COMMENT 'Ngày cập nhật',
    updated_by  VARCHAR(255) DEFAULT NULL COMMENT 'Người cập nhật',
    is_active   TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Trạng thái hoạt động',

    PRIMARY KEY (id),
    UNIQUE KEY idx_users_username (username),
    UNIQUE KEY idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────
-- 2. user_token
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_token (
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính',
    user_id            BIGINT       NOT NULL COMMENT 'Khóa ngoại trỏ đến người dùng',
    access_token       TEXT         DEFAULT NULL COMMENT 'Token truy cập',
    refresh_token      TEXT         DEFAULT NULL COMMENT 'Token làm mới',
    device_ip          VARCHAR(50)  DEFAULT NULL COMMENT 'IP thiết bị đăng nhập',
    user_agent         VARCHAR(500) DEFAULT NULL COMMENT 'Trình duyệt/Thiết bị',
    access_expires_at  TIMESTAMP    NULL DEFAULT NULL COMMENT 'Thời điểm hết hạn Access Token',
    refresh_expires_at TIMESTAMP    NULL DEFAULT NULL COMMENT 'Thời điểm hết hạn Refresh Token',
    is_revoked         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'Cờ vô hiệu hóa token',
    revoked_at         TIMESTAMP    NULL DEFAULT NULL COMMENT 'Thời điểm vô hiệu hóa',
    revoke_reason      VARCHAR(50)  DEFAULT NULL COMMENT 'Lý do bị vô hiệu hóa',

    -- BaseEntity fields
    created_at         TIMESTAMP    NULL DEFAULT NULL COMMENT 'Ngày tạo',
    created_by         VARCHAR(255) DEFAULT NULL COMMENT 'Người tạo',
    updated_at         TIMESTAMP    NULL DEFAULT NULL COMMENT 'Ngày cập nhật',
    updated_by         VARCHAR(255) DEFAULT NULL COMMENT 'Người cập nhật',
    is_active          TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Trạng thái hoạt động',

    PRIMARY KEY (id),
    INDEX idx_user_token_user_id (user_id),
    INDEX idx_user_token_access (access_token(700)),
    INDEX idx_user_token_refresh (refresh_token(700))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────
-- 3. permission_api
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS permission_api (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính',
    user_type   VARCHAR(20)  NOT NULL COMMENT 'Loại người dùng được cấp quyền',
    http_method VARCHAR(10)  NOT NULL COMMENT 'Phương thức HTTP',
    uri_pattern VARCHAR(300) NOT NULL COMMENT 'Đường dẫn API (Pattern)',
    description VARCHAR(500) DEFAULT NULL COMMENT 'Mô tả quyền hạn',

    -- BaseEntity fields
    created_at  TIMESTAMP    NULL DEFAULT NULL COMMENT 'Ngày tạo',
    created_by  VARCHAR(255) DEFAULT NULL COMMENT 'Người tạo',
    updated_at  TIMESTAMP    NULL DEFAULT NULL COMMENT 'Ngày cập nhật',
    updated_by  VARCHAR(255) DEFAULT NULL COMMENT 'Người cập nhật',
    is_active   TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Trạng thái hoạt động',

    PRIMARY KEY (id),
    INDEX idx_perm_user_type (user_type),
    INDEX idx_perm_method_uri (http_method, uri_pattern)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────
-- 4. user_addresses
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_addresses (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT 'Khóa ngoại trỏ đến bảng users',
    is_default      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'Cờ hiệu Địa chỉ mặc định hiển thị trên Profile',
    country         VARCHAR(100) DEFAULT NULL COMMENT 'Quốc gia/Vùng lãnh thổ',
    address_line    VARCHAR(255) DEFAULT NULL COMMENT 'Địa chỉ nhà/đường',
    apartment_suite VARCHAR(255) DEFAULT NULL COMMENT 'Căn hộ/Tòa nhà',
    city            VARCHAR(100) DEFAULT NULL COMMENT 'Thành phố',
    state           VARCHAR(100) DEFAULT NULL COMMENT 'Bang/Tỉnh',
    zip_code        VARCHAR(50)  DEFAULT NULL COMMENT 'Mã Bưu điện Zipcode',

    -- BaseEntity fields
    created_at      TIMESTAMP    NULL DEFAULT NULL COMMENT 'Ngày tạo',
    created_by      VARCHAR(255) DEFAULT NULL COMMENT 'Người tạo',
    updated_at      TIMESTAMP    NULL DEFAULT NULL COMMENT 'Ngày cập nhật',
    updated_by      VARCHAR(255) DEFAULT NULL COMMENT 'Người cập nhật',
    is_active       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Trạng thái hoạt động',

    PRIMARY KEY (id),
    CONSTRAINT fk_user_address_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_addresses_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────
-- 5. redis_connection_log
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS redis_connection_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính',
    operation     VARCHAR(30)  DEFAULT NULL COMMENT 'Tên thao tác (SET, GET...)',
    redis_key     VARCHAR(300) DEFAULT NULL COMMENT 'Khóa Redis bị tác động',
    status        VARCHAR(20)  DEFAULT NULL COMMENT 'Trạng thái lỗi hay thành công',
    error_message TEXT         DEFAULT NULL COMMENT 'Mô tả lỗi (Nếu có)',
    executed_at   TIMESTAMP    NULL DEFAULT NULL COMMENT 'Thời gian thực thi',

    PRIMARY KEY (id),
    INDEX idx_redis_log_executed_at (executed_at),
    INDEX idx_redis_log_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────
-- 6. rabbitmq_connection_log
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rabbitmq_connection_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính',
    exchange_name VARCHAR(200) DEFAULT NULL COMMENT 'Tên Exchange',
    queue_name    VARCHAR(200) DEFAULT NULL COMMENT 'Tên Hàng đợi Queue',
    routing_key   VARCHAR(200) DEFAULT NULL COMMENT 'Khóa định tuyến Routing Key',
    message_id    VARCHAR(100) DEFAULT NULL COMMENT 'ID của thông điệp',
    message_body  TEXT         DEFAULT NULL COMMENT 'Nội dung thông điệp',
    status        VARCHAR(30)  DEFAULT NULL COMMENT 'Trạng thái gửi/lắng nghe',
    error_message TEXT         DEFAULT NULL COMMENT 'Mô tả lỗi',
    executed_at   TIMESTAMP    NULL DEFAULT NULL COMMENT 'Thời gian thực thi',

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
    -- =================== QUYỀN CỦA ADMIN (Tuyệt đối) ===================
    ('ADMIN', 'GET',    '/film/**', 'Admin full GET access',    NOW(), 1),
    ('ADMIN', 'POST',   '/film/**', 'Admin full POST access',   NOW(), 1),
    ('ADMIN', 'PUT',    '/film/**', 'Admin full PUT access',    NOW(), 1),
    ('ADMIN', 'PATCH',  '/film/**', 'Admin full PATCH access',  NOW(), 1),
    ('ADMIN', 'DELETE', '/film/**', 'Admin full DELETE access', NOW(), 1),

    -- =================== QUYỀN CỦA MODERATOR (Quản trị nội dung) ========
    ('MODERATOR', 'GET',    '/film/**', 'Moderator full GET access',   NOW(), 1),
    ('MODERATOR', 'POST',   '/film/**', 'Moderator full POST access',  NOW(), 1),
    ('MODERATOR', 'PUT',    '/film/**', 'Moderator full PUT access',   NOW(), 1),
    ('MODERATOR', 'PATCH',  '/film/**', 'Moderator full PATCH access', NOW(), 1),
    ('MODERATOR', 'POST',   '/film/auth/v1/logout', 'Moderator logout', NOW(), 1),
    ('MODERATOR', 'PUT',    '/film/auth/v1/change-password', 'Moderator change password', NOW(), 1),

    -- =================== QUYỀN CỦA USER (Chỉ API cá nhân) ================
    ('USER', 'GET',    '/film/user/v1/**', 'Xem thông tin Profile cá nhân', NOW(), 1),
    ('USER', 'PUT',    '/film/user/v1/**', 'Cập nhật Profile cá nhân', NOW(), 1),
    ('USER', 'POST',   '/film/user/v1/**', 'Thêm mới', NOW(), 1),
    ('USER', 'DELETE', '/film/user/v1/**', 'Xóa', NOW(), 1),
    ('USER', 'PATCH',  '/film/user/v1/**', 'Cập nhật một phần', NOW(), 1),
    ('USER', 'POST',   '/film/auth/v1/logout', 'Đăng xuất', NOW(), 1),
    ('USER', 'PUT',    '/film/auth/v1/change-password', 'Đổi mật khẩu', NOW(), 1),
    ('USER', 'GET',    '/film/token/v1/**', 'Xem danh sách thiết bị/token', NOW(), 1),
    ('USER', 'DELETE', '/film/token/v1/**', 'Xóa quyền/thu hồi thiết bị', NOW(), 1);
