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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quản lý thông tin tài khoản người dùng, nhân viên, khách hàng';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lưu trữ Access/Refresh Token và phiên đăng nhập của từng thiết bị';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng phân quyền bảo mật API linh động theo Role người dùng';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Sổ địa chỉ giao hàng (Shipping) và thanh toán (Billing) của người dùng';

-- ──────────────────────────────────────────────────────────────
-- 5. media_files
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS media_files (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính',
    user_id         BIGINT       NULL COMMENT 'Tài khoản sở hữu file',
    file_type       VARCHAR(30)  NOT NULL COMMENT 'IMAGE / PDF / MOCKUP / ARTWORK',
    storage_disk    VARCHAR(30)  NOT NULL COMMENT 'LOCAL / S3 / MINIO',
    storage_path    VARCHAR(500) NOT NULL UNIQUE COMMENT 'Đường dẫn/Key lưu file',
    original_name   VARCHAR(255) NOT NULL COMMENT 'Tên gốc',
    mime_type       VARCHAR(100) NOT NULL COMMENT 'Định dạng file thật',
    file_size_bytes BIGINT       NOT NULL COMMENT 'Kích thước file',
    width_px        INT          NULL COMMENT 'Chiều rộng',
    height_px       INT          NULL COMMENT 'Chiều cao',
    dpi             INT          NULL COMMENT 'Độ phân giải',
    checksum_sha256 CHAR(64)     NULL COMMENT 'Chuỗi SHA256 chống trùng',
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DELETED',
    created_at      TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
    created_by      VARCHAR(50)  NULL COMMENT 'Người tạo',
    updated_at      TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày cập nhật',
    updated_by      VARCHAR(50)  NULL COMMENT 'Người sửa cuối',
    is_active       TINYINT(1)   NULL DEFAULT 1 COMMENT '0: Xóa mềm, 1: Đang dùng',

    PRIMARY KEY (id),
    CONSTRAINT fk_media_file_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_media_user (user_id),
    INDEX idx_media_checksum (checksum_sha256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Kho lưu trữ thông tin tập trung về Hình ảnh, File In DTF, và Mockup';

-- ──────────────────────────────────────────────────────────────
-- 6. categories
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS categories (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính',
    parent_id     BIGINT       DEFAULT NULL COMMENT 'Danh mục cha để làm menu con (Tự tham chiếu)',
    category_type VARCHAR(30)  NOT NULL COMMENT 'Loại danh mục (PRODUCT, NEWS, BLOG, FAQ, POLICY)',
    source_system VARCHAR(30)  NOT NULL DEFAULT 'FLASH_DTF' COMMENT 'Nguồn hệ thống gốc (FLASH_DTF / POD)',
    external_id   VARCHAR(100) DEFAULT NULL COMMENT 'ID đối soát từ hệ thống POD (nếu có)',
    name          VARCHAR(150) NOT NULL COMMENT 'Tên danh mục hiển thị',
    slug          VARCHAR(180) NOT NULL UNIQUE COMMENT 'Đường dẫn URL SEO thân thiện',
    description   TEXT         DEFAULT NULL COMMENT 'Mô tả chi tiết danh mục',
    sort_order    INT          NOT NULL DEFAULT 0 COMMENT 'Vị trí thứ tự sắp xếp trên web',
    is_active     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Cờ Bật/Tắt hiển thị danh mục',
    seo_title     VARCHAR(255) DEFAULT NULL COMMENT 'Tiêu đề SEO (Trình duyệt)',
    seo_description VARCHAR(500) DEFAULT NULL COMMENT 'Mô tả thẻ Meta SEO',

    -- BaseEntity fields
    created_at    TIMESTAMP    NULL DEFAULT NULL COMMENT 'Thời điểm lập danh mục',
    created_by    VARCHAR(255) DEFAULT NULL COMMENT 'Người khởi tạo',
    updated_at    TIMESTAMP    NULL DEFAULT NULL COMMENT 'Lần cập nhật cuối',
    updated_by    VARCHAR(255) DEFAULT NULL COMMENT 'Người sửa cuối',

    PRIMARY KEY (id),
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_category_type (category_type),
    INDEX idx_category_source (source_system)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Danh mục dùng chung cho toàn bộ Product, News, Blog';

-- ──────────────────────────────────────────────────────────────
-- 7. products
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS products (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính',
    category_id      BIGINT       NOT NULL COMMENT 'Liên kết thuộc về menu thư mục ảo',
    source_system    VARCHAR(30)  NOT NULL DEFAULT 'FLASH_DTF' COMMENT 'Hệ thống xuất xứ (FLASH_DTF / POD)',
    external_id      VARCHAR(100) DEFAULT NULL COMMENT 'ID của hàng hóa tự động kéo từ POD',
    product_type     VARCHAR(40)  NOT NULL COMMENT 'Mô hình kinh doanh (SHIRT, DTF_BY_SIZE, UPLOAD_GANG_SHEET)',
    sku              VARCHAR(80)  NOT NULL UNIQUE COMMENT 'Mã quản lý tồn kho nội bộ (Bắt buộc duy nhất)',
    name             VARCHAR(200) NOT NULL COMMENT 'Tên sản phẩm',
    slug             VARCHAR(220) NOT NULL UNIQUE COMMENT 'Đường dẫn SEO (Vd: ao-hoodie-zip)',
    short_description VARCHAR(500) DEFAULT NULL COMMENT 'Lời tóm tắt quảng cáo (đoạn ngắn)',
    description_html LONGTEXT     DEFAULT NULL COMMENT 'Nội dung dính kèm hình ảnh HTML',
    status           VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT 'Trạng thái phát hành (DRAFT, ACTIVE, INACTIVE, ARCHIVED)',
    is_custom_upload TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'Cờ yêu cầu KH upload file dính kèm',
    lead_time_days   INT          DEFAULT NULL COMMENT 'Số ngày xử lý đơn vị theo chuẩn',
    seo_title        VARCHAR(255) DEFAULT NULL COMMENT 'Thẻ tiêu đề SEO',
    seo_description  VARCHAR(500) DEFAULT NULL COMMENT 'Thẻ mô tả SEO',

    -- BaseEntity fields
    created_at       TIMESTAMP    NULL DEFAULT NULL COMMENT 'Lúc đăng',
    created_by       VARCHAR(255) DEFAULT NULL COMMENT 'Trình biên tập',
    updated_at       TIMESTAMP    NULL DEFAULT NULL COMMENT 'Lúc chỉnh mới',
    updated_by       VARCHAR(255) DEFAULT NULL COMMENT 'Người chỉnh cuối',
    is_active        TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Cho phép lên kệ mua bán',

    PRIMARY KEY (id),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    INDEX idx_product_source (source_system),
    INDEX idx_product_type (product_type),
    INDEX idx_product_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Thông tin gốc của Sản phẩm kinh doanh chính';

-- ──────────────────────────────────────────────────────────────
-- 8. product_variants
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS product_variants (
    id                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính',
    product_id           BIGINT       NOT NULL COMMENT 'Thuộc về Sản phẩm gốc định trước',
    sku                  VARCHAR(100) NOT NULL UNIQUE COMMENT 'Mã SKU phân nhánh cụ thể cho biến thể này',
    option_snapshot_json JSON         NOT NULL COMMENT 'Dữ liệu Option động (VD: {"size": "M", "color": "Red", "width": 22})',
    size_label           VARCHAR(80)  DEFAULT NULL COMMENT 'Tên Size hiển thị chữ thô',
    color_label          VARCHAR(80)  DEFAULT NULL COMMENT 'Tên Màu hiển thị chữ thô',
    sort_order           INT          NOT NULL DEFAULT 0 COMMENT 'Sắp xếp hiển thị',
    status               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'Trạng thái hoạt động Biến thể (ACTIVE/INACTIVE)',

    -- BaseEntity fields
    created_at           TIMESTAMP    NULL DEFAULT NULL COMMENT 'Ngày tạo',
    created_by           VARCHAR(255) DEFAULT NULL COMMENT 'Người tạo',
    updated_at           TIMESTAMP    NULL DEFAULT NULL COMMENT 'Ngày sửa cuối',
    updated_by           VARCHAR(255) DEFAULT NULL COMMENT 'Người sửa cuối',
    is_active            TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Cờ sử dụng dữ liệu',

    PRIMARY KEY (id),
    CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_variant_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Biến thể SKU của sản phẩm theo size/color hoặc cấu hình';

-- ──────────────────────────────────────────────────────────────
-- 9. product_price_rules
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS product_price_rules (
    id                BIGINT         NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính',
    product_id        BIGINT         NOT NULL COMMENT 'Áp giá cho Sản phẩm gốc nào',
    product_variant_id BIGINT         DEFAULT NULL COMMENT 'Áp giá riêng lẻ cho từng Size (Tùy chọn)',
    price_type        VARCHAR(30)    NOT NULL COMMENT 'Loại luồng tính giá (FIXED, QTY_TIER, SHEET_LENGTH)',
    min_qty           INT            DEFAULT NULL COMMENT 'Ngưỡng mua Số lượng khối tối thiểu bắt đầu giảm',
    max_qty           INT            DEFAULT NULL COMMENT 'Ngưỡng mua Số lượng tối đa áp dụng đợt này',
    width_inch        DECIMAL(8,2)   DEFAULT NULL COMMENT 'Chiều rộng thực tế tính toán (Đo bằng Inch)',
    height_inch       DECIMAL(8,2)   DEFAULT NULL COMMENT 'Chiều cao thực tế đo bằng Inch',
    sheet_length_inch DECIMAL(8,2)   DEFAULT NULL COMMENT 'Toàn độ dài cuộn Sheet In đo bằng Inch',
    unit_price        DECIMAL(12,2)  NOT NULL COMMENT 'Đơn giá quy định tính ra Dollar',
    extra_charge      DECIMAL(12,2)  NOT NULL DEFAULT 0 COMMENT 'Phí phụ thu kèm theo (Mực đắt, công cán)',
    effective_from    DATETIME       DEFAULT NULL COMMENT 'Thời gian Kích hoạt bảng giá',
    effective_to      DATETIME       DEFAULT NULL COMMENT 'Ngày hết hạn Khuyến mãi tự hạ giá',
    is_active         TINYINT(1)     NOT NULL DEFAULT 1 COMMENT 'Luật Giá còn hiệu lực hay không',

    created_at        TIMESTAMP      NULL DEFAULT NULL COMMENT 'Ngày chốt bảng giá',
    created_by        VARCHAR(255)   DEFAULT NULL COMMENT 'Nhân viên chốt giá',
    updated_at        TIMESTAMP      NULL DEFAULT NULL COMMENT 'Cập nhật giá',
    updated_by        VARCHAR(255)   DEFAULT NULL COMMENT 'Người cập nhật giá cuối',

    PRIMARY KEY (id),
    CONSTRAINT fk_price_rule_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_price_rule_variant FOREIGN KEY (product_variant_id) REFERENCES product_variants(id) ON DELETE CASCADE,
    INDEX idx_price_rule_type (price_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Máy tính quy tắc giá/số lượng/mét dài của Flash DTF';

-- ──────────────────────────────────────────────────────────────
-- 10. product_file_rules
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS product_file_rules (
    id                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính',
    product_id           BIGINT       NOT NULL COMMENT 'Luật Upload chỉ định cho SP nào',
    allowed_extensions   VARCHAR(255) NOT NULL COMMENT 'Đuôi file đc chấp thuận (jpg, png, svg)',
    min_dpi              INT          DEFAULT NULL COMMENT 'Mật độ điểm ảnh 300 bắt buộc',
    min_width_px         INT          DEFAULT NULL COMMENT 'Độ rộng tối thiểu pixel',
    min_height_px        INT          DEFAULT NULL COMMENT 'Độ cao tối thiểu pixel',
    max_file_mb          DECIMAL(8,2) DEFAULT NULL COMMENT 'Ràng buộc Cân nặng Megabyte tải lên',
    transparent_required TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '1=Cắt bỏ nền đằng sau, 0=Cho Up Cả Khối TRắng',
    notes                VARCHAR(500) DEFAULT NULL COMMENT 'Log thông báo của QC File Checker cho khách',
    is_active            TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Luật này còn áp không',

    created_at           TIMESTAMP    NULL DEFAULT NULL COMMENT 'Ngày tạo log',
    created_by           VARCHAR(255) DEFAULT NULL COMMENT 'Tạo bởi KCS',
    updated_at           TIMESTAMP    NULL DEFAULT NULL COMMENT 'Cập nhật log',
    updated_by           VARCHAR(255) DEFAULT NULL COMMENT 'Biên tập bởi',

    PRIMARY KEY (id),
    CONSTRAINT fk_file_rule_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bộ lọc quy cách File in cho SP có chức năng Custom Upload';

-- ──────────────────────────────────────────────────────────────
-- 11. product_media
-- ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS product_media (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính',
    product_id    BIGINT       NOT NULL COMMENT 'Ánh xạ thẳng về Sản Phẩm Mẹ',
    media_file_id BIGINT       NOT NULL COMMENT 'Dẫn Link vào Bảng Media Kho Chứa Ảnh Của Bạn',
    media_role    VARCHAR(30)  NOT NULL COMMENT 'Vai diễn của Ảnh: THUMBNAIL, GALLERY, MOCKUP_GUIDE',
    sort_order    INT          NOT NULL DEFAULT 0 COMMENT 'Thứ tự vị trí tấm ảnh xuất hiện số 1, 2, 3..',
    is_primary    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'Ảnh bìa to đùng của Sản Phẩm (Đúng/Sai)',

    created_at    TIMESTAMP    NULL DEFAULT NULL COMMENT 'Ngày lên kệ đăng hình',
    created_by    VARCHAR(255) DEFAULT NULL COMMENT 'Nv Đăng hình',
    updated_at    TIMESTAMP    NULL DEFAULT NULL COMMENT 'Lần update lại hình',
    updated_by    VARCHAR(255) DEFAULT NULL COMMENT 'Nv Đổi hình',
    is_active     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Giữ Hình hay Ẩn Đi',

    PRIMARY KEY (id),
    CONSTRAINT fk_pm_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_pm_media FOREIGN KEY (media_file_id) REFERENCES media_files(id) ON DELETE CASCADE,
    INDEX idx_pm_role (media_role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cầu nối liên kết kho Ảnh cho bài Mô Tả Hàng Hóa';


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
