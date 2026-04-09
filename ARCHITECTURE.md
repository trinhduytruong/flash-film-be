# ARCHITECTURE.md — Flash DTF Backend

> **Version:** 2.0 | **Updated:** 2025-06 | **Stack:** Spring Boot 4.0 · Java 21 · MySQL 8 · Redis 7 · RabbitMQ 3

**Trạng thái ký hiệu:**
- ✅ Đã làm
- ❌ Chưa làm

---

## Table of Contents

1. [Giới thiệu](#1-giới-thiệu)
2. [Kiến trúc hệ thống (C4 Model)](#2-kiến-trúc-hệ-thống-c4-model)
3. [Luồng nghiệp vụ](#3-luồng-nghiệp-vụ)
4. [Đặc tả API](#4-đặc-tả-api)
5. [Mô tả Database](#5-mô-tả-database)
6. [Cấu hình & Infrastructure](#6-cấu-hình--infrastructure)
7. [Phụ lục](#7-phụ-lục)

---

## 1. Giới thiệu

### 1.1 Mục đích

Tài liệu mô tả thiết kế kiến trúc toàn hệ thống backend **Flash DTF** — nền tảng thương mại điện tử chuyên về sản phẩm in ấn theo yêu cầu (Print-on-Demand / DTF). Bao gồm cả phần đã triển khai và phần đang trong roadmap.

### 1.2 Tính năng hệ thống

| # | Tính năng | Giai đoạn | Trạng thái |
|---|---|---|---|
| 1 | Authentication (JWT, OTP, Refresh Token) | Phase 1 | ✅ |
| 2 | Phân quyền RBAC động (permission_api) | Phase 1 | ✅ |
| 3 | Media / File Upload (Local + S3) | Phase 1 | ✅ |
| 4 | Database schema sản phẩm (6 bảng) | Phase 2 | ✅ |
| 5 | Public API: Product, Category (GET) | Phase 3 | ✅ |
| 6 | User Profile & Address CRUD | Phase 1 | ✅ |
| 7 | Admin CRUD: Product & Category | Phase 4 | ❌ |
| 8 | Giỏ hàng (Cart & Cart Items) | Phase 5 | ❌ |
| 9 | Thanh toán & Checkout | Phase 5 | ❌ |
| 10 | Quản lý đơn hàng & Workflow xưởng in | Phase 6 | ❌ |

### 1.3 Các hệ thống liên quan

| Hệ thống | Vai trò | Trạng thái |
|---|---|---|
| Flash DTF Frontend | SPA/Mobile app gọi REST API | ✅ |
| Gmail SMTP | Gửi email OTP xác thực | ✅ |
| AWS S3 | Cloud storage file artwork | ✅ (config sẵn) |
| RabbitMQ | Message queue bất đồng bộ | ✅ (config sẵn) |
| Redis | Cache OTP, blacklist token | ✅ |
| MySQL 8 | Database chính | ✅ |
| Payment Gateway (VNPay/Stripe) | Thanh toán online | ❌ |

### 1.4 Định nghĩa & Thuật ngữ

| Thuật ngữ | Định nghĩa |
|---|---|
| DTF | Direct To Film — kỹ thuật in chuyển nhiệt lên vải |
| JWT | JSON Web Token — xác thực stateless |
| RBAC | Role-Based Access Control |
| SKU | Stock Keeping Unit — mã hàng tồn kho |
| OTP | One-Time Password |
| Gang Sheet | Tờ in tập hợp nhiều hình ảnh nhỏ |
| Access Token | JWT ngắn hạn (15 phút) |
| Refresh Token | JWT dài hạn (7 ngày) |
| ADMIN / MODERATOR / USER | Các role trong hệ thống |
| DRAFT/ACTIVE/ARCHIVED | Trạng thái vòng đời sản phẩm |

---

## 2. Kiến trúc hệ thống (C4 Model)

### 2.1 Context Diagram

```
                    ┌──────────────┐
                    │  End User    │
                    │  (Customer)  │
                    └──────┬───────┘
                           │
                    ┌──────┴───────┐         ┌──────────────────┐
                    │ Flash DTF FE │         │  Admin / Internal │
                    │(SPA / Mobile)│         │  Dashboard        │
                    └──────┬───────┘         └────────┬──────────┘
                           │  REST/HTTPS              │ REST/HTTPS
                           └──────────────┬───────────┘
                                          │
              ┌───────────────────────────▼──────────────────────────────┐
              │              ██ FILM BE (Spring Boot) ██                 │
              │                   http://localhost:8080                  │
              │          REST API · JWT · RBAC · JPA · Scheduler         │
              └──────┬────────┬──────────┬──────────┬────────────────────┘
                     │        │          │          │
           ┌─────────┴┐  ┌───┴──┐  ┌───┴───┐  ┌──┴──────────┐
           │ MySQL 8  │  │Redis │  │Rabbit │  │  AWS S3      │
           │✅        │  │✅    │  │MQ ✅  │  │✅(config)    │
           └──────────┘  └──────┘  └───────┘  └─────────────┘
                                                        ┌────────────────┐
                                              ❌         │Payment Gateway │
                                                        │(VNPay/Stripe)  │
                                                        └────────────────┘
```

| # | Hệ thống | Mô tả | Trạng thái |
|---|---|---|---|
| 1 | Film BE | Spring Boot application — core backend | ✅ |
| 2 | Flash DTF Frontend | SPA hoặc mobile app | ✅ |
| 3 | Gmail SMTP | Gửi OTP qua email | ✅ |
| 4 | AWS S3 | Lưu file artwork, hình ảnh | ✅ config |
| 5 | RabbitMQ | Message broker bất đồng bộ | ✅ config |
| 6 | Redis | Cache OTP, blacklist token | ✅ |
| 7 | Payment Gateway | Thanh toán online (VNPay/Stripe) | ❌ |

---

### 2.2 Container Diagram

```
┌───────────────────────────────────────────────────────────────────────────┐
│                        FILM BE — Container View                           │
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────────┐ │
│  │                    Spring Boot Application ✅                        │ │
│  │                                                                      │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐             │ │
│  │  │  Auth ✅ │  │ User ✅  │  │Product ✅│  │ Media ✅│             │ │
│  │  └──────────┘  └──────────┘  └──────────┘  └─────────┘             │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐             │ │
│  │  │Permis. ✅│  │ Token ✅ │  │Category ✅│  │  Log ✅ │             │ │
│  │  └──────────┘  └──────────┘  └──────────┘  └─────────┘             │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐                           │ │
│  │  │Admin     │  │  Cart    │  │  Order   │  ← ❌ Chưa làm            │ │
│  │  │CRUD ❌   │  │  ❌      │  │  ❌      │                           │ │
│  │  └──────────┘  └──────────┘  └──────────┘                           │ │
│  └──────────────────────────────────────────────────────────────────────┘ │
│          │              │               │              │                   │
│          ▼              ▼               ▼              ▼                   │
│   ┌────────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐           │
│   │ MySQL 8 ✅ │  │ Redis ✅ │  │RabbitMQ ✅│  │  AWS S3 ✅   │           │
│   │ :3309      │  │ :6379    │  │ :5672    │  │  (cloud)     │           │
│   └────────────┘  └──────────┘  └──────────┘  └──────────────┘           │
│                                                                           │
│                                              ┌──────────────┐            │
│                                              │Payment GW ❌ │            │
│                                              └──────────────┘            │
└───────────────────────────────────────────────────────────────────────────┘
```

| # | Module | Trạng thái | Mô tả |
|---|---|---|---|
| 1 | Auth | ✅ | Login, Register, OTP, Refresh, Change/Reset Password |
| 2 | User | ✅ | Profile, Address CRUD |
| 3 | Product | ✅ (Public), ❌ (Admin) | Public GET xong, Admin CRUD chưa |
| 4 | Category | ✅ (Public), ❌ (Admin) | Public GET xong, Admin CRUD chưa |
| 5 | Media | ✅ | Upload file Local/S3 |
| 6 | Permission | ✅ | RBAC load động từ DB |
| 7 | Token | ✅ | Quản lý phiên đăng nhập |
| 8 | Log | ✅ | Access log |
| 9 | Admin CRUD | ❌ | CRUD Product + Category cho Admin |
| 10 | Cart | ❌ | Giỏ hàng, tính giá động |
| 11 | Order | ❌ | Đơn hàng, workflow xưởng in |
| 12 | Payment | ❌ | Tích hợp cổng thanh toán |

---

### 2.3 Component Diagram — Spring Boot App

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      Spring Boot Application                            │
│                                                                         │
│  HTTP Request                                                           │
│       │                                                                 │
│       ▼                                                                 │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                Security Filter Chain ✅                           │  │
│  │    JwtAuthenticationFilter → Permission Check (permission_api)    │  │
│  └────────────────────────┬──────────────────────────────────────────┘  │
│                           │                                             │
│   ┌───────────────────────┼──────────────────────┐                     │
│   │                       │                      │                     │
│   ▼          ✅           ▼          ✅           ▼       ❌            │
│ ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────────┐    │
│ │Auth      │  │User      │  │Product   │  │Admin / Cart / Order  │    │
│ │Controller│  │Controller│  │Public    │  │Controllers           │    │
│ └────┬─────┘  └────┬─────┘  │Controller│  └──────────────────────┘    │
│      │             │        └────┬─────┘                               │
│      ▼             ▼            ▼                                      │
│ ┌──────────┐  ┌──────────┐  ┌──────────┐                              │
│ │Auth      │  │User      │  │Product   │                              │
│ │Service ✅│  │Service ✅│  │Service ✅│   ← Admin/Cart/Order ❌      │
│ └────┬─────┘  └────┬─────┘  └────┬─────┘                              │
│      │             │              │                                     │
│      ▼             ▼              ▼                                     │
│ ┌─────────────────────────────────────────────────────────────────┐    │
│ │              Repository Layer (Spring Data JPA) ✅              │    │
│ └────────────────────────┬────────────────────────────────────────┘    │
│                          │                                              │
│        ┌─────────────────┼───────────────────┐                         │
│        ▼                 ▼                   ▼                         │
│   ┌──────────┐     ┌──────────┐       ┌──────────┐                     │
│   │MySQL DB ✅│    │Redis ✅  │       │RabbitMQ ✅│                    │
│   └──────────┘     └──────────┘       └──────────┘                     │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │             Common / Cross-cutting Layer ✅                      │   │
│  │  ApiResponse · AppCode · GlobalExceptionHandler                  │   │
│  │  JwtUtil · CookieUtil · AuditingConfig · SlugUtil ❌             │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

| # | Component | Trạng thái | Mô tả |
|---|---|---|---|
| 1 | JwtAuthenticationFilter | ✅ | Validate JWT, check permission từ DB |
| 2 | SecurityConfig | ✅ | Filter chain, public paths |
| 3 | AuthController | ✅ | Login, Register, Logout, Refresh, OTP |
| 4 | UserController | ✅ | Profile, Address CRUD |
| 5 | ProductPublicController | ✅ | GET list, GET detail |
| 6 | CategoryPublicController | ✅ | GET tree/list |
| 7 | FileUploadController | ✅ | Upload file Local/S3 |
| 8 | UserTokenController | ✅ | Quản lý phiên đăng nhập |
| 9 | GlobalExceptionHandler | ✅ | Xử lý lỗi tập trung |
| 10 | ProductAdminController | ❌ | POST/PUT/DELETE/PATCH admin |
| 11 | CategoryAdminController | ❌ | POST/PUT/DELETE admin |
| 12 | CartController | ❌ | Thêm/sửa/xóa item giỏ hàng |
| 13 | CheckoutController | ❌ | Checkout, áp voucher |
| 14 | OrderController | ❌ | Tạo đơn, xem đơn, cập nhật trạng thái |
| 15 | SlugUtil | ❌ | Auto generate slug từ name |

---

## 3. Luồng nghiệp vụ

### 3.1 Đăng ký tài khoản ✅

```
Client        AuthController     AuthService        Redis       MailService      DB
  │                 │                 │               │              │            │
  │─1. POST /send-register-otp───────>│               │              │            │
  │                 │──2. delegate───>│               │              │            │
  │                 │                 │──3. check email dup──────────────────────>│
  │                 │                 │──4. gen OTP──>│              │            │
  │                 │                 │   TTL=5min    │              │            │
  │                 │                 │──5. send OTP──────────────────────────── >│
  │<─6. 200 OK─────│                 │               │              │            │
  │                 │                 │               │              │            │
  │─7. POST /register {email,otp}────>│               │              │            │
  │                 │──8. delegate───>│               │              │            │
  │                 │                 │──9. verify OTP>│             │            │
  │                 │                 │──10. hash pw, INSERT user────────────────>│
  │                 │                 │──11. delete OTP>│            │            │
  │<─12. 201 Created {id,email}──────│                │             │            │
```

### 3.2 Đăng nhập ✅

```
Client        AuthController     AuthService        DB            Redis
  │                 │                 │              │               │
  │─1. POST /login {email,pw}────────>│              │               │
  │                 │──2. delegate───>│              │               │
  │                 │                 │──3. findByEmail─────────────>│
  │                 │                 │──4. BCrypt verify password   │
  │                 │                 │──5. sign JWT (per-user secret)│
  │                 │                 │──6. save user_token──────────>│
  │                 │                 │──7. update last_login────────>│
  │<─8. 200 {accessToken}            │              │               │
  │   Set-Cookie: refresh_token(HttpOnly)            │               │
```

### 3.3 Xác thực request (JWT Filter) ✅

```
Client       JwtAuthFilter      JwtUtil       PermissionService    Controller
  │                │               │                 │                  │
  │─1. Request + Bearer──────────>│               │                   │
  │                │──2. extract──>│               │                   │
  │                │<──claims──────│               │                   │
  │                │──3. load user─────────────────────────────── DB   │
  │                │──4. check permission──────────>│                   │
  │                │<──ALLOW/DENY──────────────────│                   │
  │                │──5. set SecurityContext────────────────────────── >│
  │<───────────────────────────────────────────── 6. Response          │
```

### 3.4 Upload file ✅

```
Client       FileUploadController    MediaFileService     Storage(Local/S3)    DB
  │                   │                    │                    │              │
  │─1. POST /upload (multipart)───────────>│                    │              │
  │                   │──2. validate───────│                    │              │
  │                   │   mime, size, DPI  │                    │              │
  │                   │                    │──3a. Local save───>│              │
  │                   │                    │──3b. S3 upload────>│              │
  │                   │                    │──4. INSERT media_files────────────>│
  │<─5. 200 {fileId, url, size, dpi}──────────────────────────────────────────│
```

### 3.5 Admin tạo/sửa sản phẩm ❌

```
Admin         ProductAdminController    ProductService(@Transactional)    DB
  │                   │                          │                        │
  │─1. POST /admin/products {ProductSaveRequest}>│                        │
  │                   │──2. validate request─────│                        │
  │                   │                          │─3. INSERT products────>│
  │                   │                          │─4. INSERT variants (N)>│
  │                   │                          │─5. INSERT price_rules─>│
  │                   │                          │─6. INSERT file_rules──>│
  │                   │                          │─7. INSERT product_media>│
  │<─8. 201 {ProductDetailResponse}──────────────────────────────────────│
  │                   │                          │                        │
  │─9. PUT /admin/products/{id} {ProductSaveRequest}─>│                  │
  │                   │                          │─10. soft-delete children cũ──>│
  │                   │                          │    (is_active=0)       │
  │                   │                          │─11. re-insert mới─────>│
  │<─12. 200 {ProductDetailResponse}─────────────────────────────────────│
  │                   │                          │                        │
  │─13. DELETE /admin/products {ids:[1,2,3]}─────>│                       │
  │                   │                          │─14. UPDATE status=ARCHIVED──>│
  │<─15. 200 OK───────────────────────────────────────────────────────────│
```

### 3.6 Giỏ hàng & Checkout ❌

```
Customer      CartController      CartService          ProductService     DB
  │                │                   │                     │           │
  │─1. POST /cart/items {variantId, qty, fileId}────────────>│           │
  │                │──2. validate──────│                     │           │
  │                │                   │──3. load product, price rule────>│
  │                │                   │──4. validate file rule (nếu custom_upload)
  │                │                   │──5. calculate price snapshot     │
  │                │                   │──6. INSERT/UPDATE cart_items────>│
  │<─7. 200 {CartResponse}────────────────────────────────────────────────│
  │                │                   │                     │           │
  │─8. POST /checkout {addressId, voucherId}────────────────>│           │
  │                │                   │──9. validate voucher────────────>│
  │                │                   │──10. create order + order_items─>│
  │                │                   │──11. update cart status=CHECKED_OUT
  │<─12. 200 {OrderResponse, paymentUrl}──────────────────────────────────│
```

### 3.7 Workflow đơn hàng tại xưởng in ❌

```
Lifecycle trạng thái đơn hàng:

PENDING_PAYMENT ──(thanh toán)──> PENDING_FILE_CHECK
                                         │
                              ┌──────────┴──────────┐
                         (file OK)              (file lỗi)
                              │                      │
                    FILE_APPROVED              FILE_REJECTED
                              │                      │
                         PRINTING              (KH re-upload)
                              │
                        LAMINATING
                              │
                         PACKAGING
                              │
                   ┌──────────┴──────────┐
              (giao thành công)    (giao thất bại)
                   │                     │
               DELIVERED           DELIVERY_FAILED
```

---

## 4. Đặc tả API

### 4.1 Danh sách API

#### Auth Module ✅

| Method | URI | Quyền | Mô tả | Status |
|---|---|---|---|---|
| POST | `/film/auth/v1/send-register-otp` | Public | Gửi OTP đăng ký | ✅ |
| POST | `/film/auth/v1/register` | Public | Đăng ký tài khoản | ✅ |
| POST | `/film/auth/v1/login` | Public | Đăng nhập | ✅ |
| POST | `/film/auth/v1/logout` | USER+ | Đăng xuất | ✅ |
| POST | `/film/auth/v1/refresh` | Public (Cookie) | Làm mới Access Token | ✅ |
| PUT | `/film/auth/v1/change-password` | USER+ | Đổi mật khẩu | ✅ |
| POST | `/film/auth/v1/send-forgot-password-otp` | Public | OTP quên mật khẩu | ✅ |
| POST | `/film/auth/v1/reset-password` | Public | Reset mật khẩu | ✅ |

#### User Module ✅

| Method | URI | Quyền | Mô tả | Status |
|---|---|---|---|---|
| GET | `/film/user/v1/profile` | USER+ | Lấy profile | ✅ |
| PUT | `/film/user/v1/profile` | USER+ | Cập nhật profile | ✅ |
| GET | `/film/user/v1/addresses` | USER+ | Danh sách địa chỉ | ✅ |
| POST | `/film/user/v1/addresses` | USER+ | Thêm địa chỉ | ✅ |
| PUT | `/film/user/v1/addresses/{id}` | USER+ | Sửa địa chỉ | ✅ |
| DELETE | `/film/user/v1/addresses/{id}` | USER+ | Xóa địa chỉ | ✅ |
| PATCH | `/film/user/v1/addresses/default/{id}` | USER+ | Đặt địa chỉ mặc định | ✅ |

#### Public Catalog ✅

| Method | URI | Quyền | Mô tả | Status |
|---|---|---|---|---|
| GET | `/film/public/v1/categories` | Public | Danh sách danh mục | ✅ |
| GET | `/film/public/v1/products` | Public | Danh sách sản phẩm (filter, paging) | ✅ |
| GET | `/film/public/v1/products/{slug}` | Public | Chi tiết sản phẩm (full JSON) | ✅ |

#### Media Module ✅

| Method | URI | Quyền | Mô tả | Status |
|---|---|---|---|---|
| POST | `/film/media/v1/upload` | USER+ | Upload file | ✅ |

#### Token Module ✅

| Method | URI | Quyền | Mô tả | Status |
|---|---|---|---|---|
| GET | `/film/token/v1/**` | USER+ | Danh sách phiên đăng nhập | ✅ |
| DELETE | `/film/token/v1/**` | USER+ | Thu hồi token | ✅ |

#### Admin Product ❌

| Method | URI | Quyền | Mô tả | Status |
|---|---|---|---|---|
| POST | `/film/admin/v1/products` | ADMIN/MOD | Tạo sản phẩm (kèm variants/prices/media) | ❌ |
| PUT | `/film/admin/v1/products/{id}` | ADMIN/MOD | Sửa sản phẩm | ❌ |
| DELETE | `/film/admin/v1/products` | ADMIN/MOD | Xóa nhiều (body: `{ids:[]}`) | ❌ |
| PATCH | `/film/admin/v1/products/{id}/status` | ADMIN/MOD | Toggle trạng thái | ❌ |

#### Admin Category ❌

| Method | URI | Quyền | Mô tả | Status |
|---|---|---|---|---|
| POST | `/film/admin/v1/categories` | ADMIN/MOD | Tạo danh mục | ❌ |
| PUT | `/film/admin/v1/categories/{id}` | ADMIN/MOD | Sửa danh mục | ❌ |
| DELETE | `/film/admin/v1/categories` | ADMIN/MOD | Xóa nhiều (body: `{ids:[]}`) | ❌ |
| PATCH | `/film/admin/v1/categories/{id}/sort` | ADMIN/MOD | Cập nhật sort order | ❌ |

#### Cart Module ❌

| Method | URI | Quyền | Mô tả | Status |
|---|---|---|---|---|
| GET | `/film/cart/v1` | USER | Xem giỏ hàng hiện tại | ❌ |
| POST | `/film/cart/v1/items` | USER | Thêm sản phẩm vào giỏ | ❌ |
| PUT | `/film/cart/v1/items/{id}` | USER | Cập nhật số lượng | ❌ |
| DELETE | `/film/cart/v1/items` | USER | Xóa nhiều item (body: `{ids:[]}`) | ❌ |
| DELETE | `/film/cart/v1` | USER | Xóa toàn bộ giỏ hàng | ❌ |

#### Checkout & Order ❌

| Method | URI | Quyền | Mô tả | Status |
|---|---|---|---|---|
| POST | `/film/checkout/v1` | USER | Chốt đơn, tạo payment | ❌ |
| GET | `/film/order/v1` | USER | Danh sách đơn hàng của user | ❌ |
| GET | `/film/order/v1/{id}` | USER | Chi tiết đơn hàng | ❌ |
| GET | `/film/admin/v1/orders` | ADMIN/MOD | Danh sách tất cả đơn | ❌ |
| PATCH | `/film/admin/v1/orders/{id}/status` | ADMIN/MOD | Cập nhật trạng thái đơn | ❌ |

---

### 4.2 Cấu trúc Response chuẩn ✅

```json
{
  "success": true,
  "httpCode": 200,
  "code": 1001,
  "message": "Login successful",
  "data": { "..." },
  "timestamp": "2025-06-02T10:30:00"
}
```

Response lỗi bulk delete category (❌ — chưa làm):
```json
{
  "success": false,
  "httpCode": 400,
  "code": 4000,
  "message": "Cannot delete categories that still have active products",
  "data": { "blockedIds": [1, 5] }
}
```

---

### 4.3 Bảng mã lỗi (AppCode) ✅

| AppCode | Code | HTTP | Tiếng Anh | Tiếng Việt |
|---|---|---|---|---|
| SUCCESS | 1000 | 200 | Success | Thành công |
| LOGIN_SUCCESS | 1001 | 200 | Login successful | Đăng nhập thành công |
| LOGOUT_SUCCESS | 1002 | 200 | Logout successful | Đăng xuất thành công |
| TOKEN_REFRESHED | 1003 | 200 | Token refreshed | Làm mới token thành công |
| REGISTER_SUCCESS | 1004 | 201 | Registration successful | Đăng ký thành công |
| CHANGE_PASSWORD_SUCCESS | 1005 | 200 | Password changed | Đổi mật khẩu thành công |
| VALIDATION_ERROR | 4000 | 400 | Validation error | Lỗi xác thực |
| UNAUTHORIZED | 4001 | 401 | Unauthorized | Chưa xác thực |
| TOKEN_EXPIRED | 4002 | 401 | Token expired | Token hết hạn |
| TOKEN_INVALID | 4003 | 401 | Token invalid | Token không hợp lệ |
| REFRESH_TOKEN_INVALID | 4004 | 401 | Refresh token invalid | Refresh token không hợp lệ |
| FORBIDDEN | 4010 | 403 | Access denied | Không có quyền |
| INVALID_CREDENTIALS | 4011 | 401 | Invalid credentials | Sai email/mật khẩu |
| USER_NOT_FOUND | 4012 | 404 | User not found | Không tìm thấy user |
| USERNAME_EXISTS | 4015 | 409 | Username exists | Tên đăng nhập đã tồn tại |
| EMAIL_EXISTS | 4016 | 409 | Email exists | Email đã tồn tại |
| WRONG_PASSWORD | 4017 | 400 | Wrong password | Sai mật khẩu hiện tại |
| NOT_FOUND | 4040 | 404 | Not found | Không tìm thấy |
| INTERNAL_ERROR | 5000 | 500 | Internal error | Lỗi hệ thống |

---

## 5. Mô tả Database

### 5.1 ERD

```
users ──────────────────────────── user_token
  │  (1:N)                            (N:1)
  │
  ├── user_addresses (1:N)
  │
  └── media_files (1:N)
        │
        └── product_media (N:1) ──── products (N:1) ── categories (self-ref)
                                         │
                              ┌──────────┼──────────────────┐
                              │          │                  │
                    product_variants  product_price_rules  product_file_rules
                              │
                    product_price_rules (via variant_id)

── ── ── ── ── ── ── ── [CHƯA LÀM] ── ── ── ── ── ── ── ──

users ──── carts ──── cart_items ──── products/variants
             │                             │
           orders ─── order_items ─── products/variants
             │
           order_status_logs
             │
           vouchers (áp dụng khi checkout)
```

---

### 5.2 DB Schema

#### ✅ Bảng: `users`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Khóa chính |
| username | VARCHAR(100) | NOT NULL, UNIQUE | Tên đăng nhập |
| email | VARCHAR(150) | NOT NULL, UNIQUE | Email |
| password | VARCHAR(255) | NOT NULL | BCrypt hash |
| first_name | VARCHAR(100) | NULL | Tên |
| last_name | VARCHAR(100) | NULL | Họ |
| phone_number | VARCHAR(20) | NULL | Số điện thoại |
| gender | VARCHAR(20) | NULL | MALE/FEMALE/OTHER |
| date_of_birth | DATE | NULL | Ngày sinh |
| user_type | VARCHAR(20) | DEFAULT 'USER' | ADMIN/MODERATOR/USER |
| jwt_secret | VARCHAR(255) | NOT NULL | Secret ký JWT riêng |
| last_login | TIMESTAMP | NULL | Lần đăng nhập cuối |
| is_active | TINYINT(1) | DEFAULT 1 | Trạng thái tài khoản |

#### ✅ Bảng: `user_token`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| user_id | BIGINT | FK → users | Người dùng |
| access_token | TEXT | NULL | JWT Access Token |
| refresh_token | TEXT | NULL | JWT Refresh Token |
| device_ip | VARCHAR(50) | NULL | IP thiết bị |
| user_agent | VARCHAR(500) | NULL | Trình duyệt/thiết bị |
| access_expires_at | TIMESTAMP | NULL | Hết hạn access |
| refresh_expires_at | TIMESTAMP | NULL | Hết hạn refresh |
| is_revoked | TINYINT(1) | DEFAULT 0 | Đã thu hồi chưa |
| revoke_reason | VARCHAR(50) | NULL | Lý do thu hồi |

#### ✅ Bảng: `user_addresses`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| user_id | BIGINT | FK → users CASCADE | Chủ sở hữu |
| is_default | TINYINT(1) | DEFAULT 0 | Địa chỉ mặc định |
| country | VARCHAR(100) | NULL | Quốc gia |
| address_line | VARCHAR(255) | NULL | Địa chỉ |
| city | VARCHAR(100) | NULL | Thành phố |
| state | VARCHAR(100) | NULL | Bang/Tỉnh |
| zip_code | VARCHAR(50) | NULL | Mã bưu chính |

#### ✅ Bảng: `permission_api`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| user_type | VARCHAR(20) | NOT NULL | ADMIN/MODERATOR/USER |
| http_method | VARCHAR(10) | NOT NULL | GET/POST/PUT/PATCH/DELETE |
| uri_pattern | VARCHAR(300) | NOT NULL | Pattern URI (`**` wildcard) |
| description | VARCHAR(500) | NULL | Mô tả quyền |
| is_active | TINYINT(1) | DEFAULT 1 | Bật/Tắt |

#### ✅ Bảng: `media_files`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| user_id | BIGINT | FK → users SET NULL | Chủ sở hữu |
| file_type | VARCHAR(30) | NOT NULL | IMAGE/PDF/MOCKUP/ARTWORK |
| storage_disk | VARCHAR(30) | NOT NULL | LOCAL/S3/MINIO |
| storage_path | VARCHAR(500) | NOT NULL, UNIQUE | Đường dẫn/key |
| original_name | VARCHAR(255) | NOT NULL | Tên gốc |
| mime_type | VARCHAR(100) | NOT NULL | MIME type |
| file_size_bytes | BIGINT | NOT NULL | Kích thước bytes |
| width_px / height_px | INT | NULL | Kích thước ảnh |
| dpi | INT | NULL | Độ phân giải |
| checksum_sha256 | CHAR(64) | NULL | Hash chống trùng lặp |
| status | VARCHAR(20) | DEFAULT 'ACTIVE' | ACTIVE/DELETED |

#### ✅ Bảng: `categories`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| parent_id | BIGINT | FK → categories self-ref | Danh mục cha |
| category_type | VARCHAR(30) | NOT NULL | PRODUCT/NEWS/BLOG/FAQ |
| name | VARCHAR(150) | NOT NULL | Tên danh mục |
| slug | VARCHAR(180) | UNIQUE | URL SEO |
| sort_order | INT | DEFAULT 0 | Thứ tự sắp xếp |
| seo_title | VARCHAR(255) | NULL | Tiêu đề SEO |
| seo_description | VARCHAR(500) | NULL | Mô tả SEO |

#### ✅ Bảng: `products`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| category_id | BIGINT | FK → categories RESTRICT | Danh mục |
| product_type | VARCHAR(40) | NOT NULL | SHIRT/DTF_BY_SIZE/UPLOAD_GANG_SHEET |
| sku | VARCHAR(80) | NOT NULL, UNIQUE | Mã SKU |
| name | VARCHAR(200) | NOT NULL | Tên sản phẩm |
| slug | VARCHAR(220) | NOT NULL, UNIQUE | URL SEO |
| status | VARCHAR(20) | DEFAULT 'DRAFT' | DRAFT/ACTIVE/INACTIVE/ARCHIVED |
| is_custom_upload | TINYINT(1) | DEFAULT 0 | Yêu cầu upload file |
| lead_time_days | INT | NULL | Số ngày xử lý |

#### ✅ Bảng: `product_variants`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| product_id | BIGINT | FK → products CASCADE | Sản phẩm gốc |
| sku | VARCHAR(100) | NOT NULL, UNIQUE | SKU biến thể |
| option_snapshot_json | JSON | NOT NULL | `{"size":"M","color":"Red"}` |
| size_label | VARCHAR(80) | NULL | Label size |
| color_label | VARCHAR(80) | NULL | Label màu |
| sort_order | INT | DEFAULT 0 | Thứ tự |
| status | VARCHAR(20) | DEFAULT 'ACTIVE' | ACTIVE/INACTIVE |

#### ✅ Bảng: `product_price_rules`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| product_id | BIGINT | FK → products CASCADE | Sản phẩm |
| product_variant_id | BIGINT | FK → variants CASCADE, NULL | Biến thể riêng |
| price_type | VARCHAR(30) | NOT NULL | FIXED/QTY_TIER/SHEET_LENGTH |
| min_qty / max_qty | INT | NULL | Ngưỡng số lượng |
| unit_price | DECIMAL(12,2) | NOT NULL | Đơn giá (USD) |
| extra_charge | DECIMAL(12,2) | DEFAULT 0 | Phí phụ thu |
| effective_from / effective_to | DATETIME | NULL | Thời gian hiệu lực |

#### ✅ Bảng: `product_file_rules`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| product_id | BIGINT | FK → products CASCADE | Sản phẩm áp luật |
| allowed_extensions | VARCHAR(255) | NOT NULL | jpg, png, svg... |
| min_dpi | INT | NULL | DPI tối thiểu (thường 300) |
| min_width_px / min_height_px | INT | NULL | Kích thước tối thiểu |
| max_file_mb | DECIMAL(8,2) | NULL | Dung lượng tối đa |
| transparent_required | TINYINT(1) | DEFAULT 0 | Bắt buộc nền trong suốt |

#### ✅ Bảng: `product_media`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| product_id | BIGINT | FK → products CASCADE | Sản phẩm |
| media_file_id | BIGINT | FK → media_files CASCADE | File ảnh |
| media_role | VARCHAR(30) | NOT NULL | THUMBNAIL/GALLERY/MOCKUP_GUIDE |
| sort_order | INT | DEFAULT 0 | Thứ tự ảnh |
| is_primary | TINYINT(1) | DEFAULT 0 | Ảnh bìa chính |

---

#### ❌ Bảng: `carts` (Chưa tạo)

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| user_id | BIGINT | FK → users | Chủ giỏ hàng |
| status | VARCHAR(20) | DEFAULT 'ACTIVE' | ACTIVE/CHECKED_OUT/ABANDONED |
| created_at / updated_at | TIMESTAMP | | |

#### ❌ Bảng: `cart_items` (Chưa tạo)

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| cart_id | BIGINT | FK → carts CASCADE | Giỏ hàng |
| product_id | BIGINT | FK → products | Sản phẩm |
| variant_id | BIGINT | FK → product_variants, NULL | Biến thể |
| quantity | INT | NOT NULL | Số lượng |
| custom_file_id | BIGINT | FK → media_files, NULL | File artwork của KH |
| unit_price_snapshot | DECIMAL(12,2) | NOT NULL | Giá tại thời điểm thêm |
| file_rule_valid | TINYINT(1) | NULL | File có đạt chuẩn không |

#### ❌ Bảng: `vouchers` (Chưa tạo)

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| code | VARCHAR(50) | NOT NULL, UNIQUE | Mã voucher |
| discount_type | VARCHAR(20) | NOT NULL | PERCENT/FIXED_AMOUNT |
| discount_value | DECIMAL(12,2) | NOT NULL | Giá trị giảm |
| min_order_value | DECIMAL(12,2) | NULL | Đơn tối thiểu |
| max_uses | INT | NULL | Số lần dùng tối đa |
| used_count | INT | DEFAULT 0 | Đã dùng bao nhiêu lần |
| expires_at | DATETIME | NULL | Hạn sử dụng |
| is_active | TINYINT(1) | DEFAULT 1 | Còn hoạt động không |

#### ❌ Bảng: `orders` (Chưa tạo)

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| user_id | BIGINT | FK → users | Khách hàng |
| status | VARCHAR(30) | NOT NULL | Xem lifecycle bên dưới |
| total_amount | DECIMAL(12,2) | NOT NULL | Tổng tiền |
| shipping_address_snapshot | JSON | NOT NULL | Snapshot địa chỉ giao |
| voucher_id | BIGINT | FK → vouchers, NULL | Voucher áp dụng |
| discount_amount | DECIMAL(12,2) | DEFAULT 0 | Số tiền được giảm |
| note | TEXT | NULL | Ghi chú của KH |
| payment_status | VARCHAR(20) | DEFAULT 'PENDING' | PENDING/PAID/REFUNDED |
| payment_ref | VARCHAR(200) | NULL | Mã giao dịch từ payment GW |

**Order status lifecycle:**
```
PENDING_PAYMENT → PENDING_FILE_CHECK → FILE_APPROVED → PRINTING
→ LAMINATING → PACKAGING → SHIPPED → DELIVERED
                                    ↘ DELIVERY_FAILED
FILE_APPROVED ← FILE_REJECTED (KH re-upload)
```

#### ❌ Bảng: `order_items` (Chưa tạo)

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| order_id | BIGINT | FK → orders CASCADE | Đơn hàng |
| product_id | BIGINT | FK → products | Sản phẩm |
| variant_id | BIGINT | FK → variants, NULL | Biến thể |
| quantity | INT | NOT NULL | Số lượng |
| unit_price | DECIMAL(12,2) | NOT NULL | Đơn giá chốt |
| custom_file_id | BIGINT | FK → media_files, NULL | File artwork |
| product_snapshot | JSON | NOT NULL | Snapshot thông tin SP |

#### ❌ Bảng: `order_status_logs` (Chưa tạo)

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | Khóa chính |
| order_id | BIGINT | FK → orders | Đơn hàng |
| from_status | VARCHAR(30) | NULL | Trạng thái trước |
| to_status | VARCHAR(30) | NOT NULL | Trạng thái sau |
| changed_by | VARCHAR(100) | NULL | Ai thay đổi (username) |
| note | TEXT | NULL | Ghi chú |
| created_at | TIMESTAMP | NOT NULL | Thời điểm đổi |

---

## 6. Cấu hình & Infrastructure

### Stack công nghệ

| Công nghệ | Phiên bản | Mục đích | Status |
|---|---|---|---|
| Java | 21 (LTS) | Runtime | ✅ |
| Spring Boot | 4.0.5 | Framework chính | ✅ |
| Spring Security | 6.x | Auth & RBAC | ✅ |
| JJWT | 0.12.6 | JWT | ✅ |
| MySQL | 8.0 | Database | ✅ |
| Redis | 7 (Alpine) | Cache & OTP | ✅ |
| RabbitMQ | 3 (Management) | Message queue | ✅ |
| SpringDoc OpenAPI | 2.8.4 | API docs | ✅ |
| AWS SDK v2 | 2.20.162 | S3 storage | ✅ config |
| Thymeleaf | — | Email template | ✅ |
| Payment SDK | — | VNPay/Stripe | ❌ |

### Ports

| Service | Port | Status |
|---|---|---|
| Spring Boot App | 8080 | ✅ |
| MySQL | 3309 (host) → 3306 | ✅ |
| Redis | 6379 | ✅ |
| RabbitMQ AMQP | 5672 | ✅ |
| RabbitMQ Management | 15672 | ✅ |

### Token Config

| Cấu hình | Giá trị |
|---|---|
| Access Token TTL | 15 phút (900,000 ms) |
| Refresh Token TTL | 7 ngày (604,800,000 ms) |
| Refresh Enabled | true |

### Storage Config

| Cấu hình | Giá trị |
|---|---|
| Max file size | 100 MB |
| Allowed MIME | png, tiff, pdf, jpeg, webp |
| Mode | Local (mặc định), S3 (toggle) |

### Scheduler

| Job | Cron | Mô tả | Status |
|---|---|---|---|
| Token Cleanup | `0 0 0 * * ?` | Xóa token hết hạn quá 5 ngày | ✅ |

---

## 7. Phụ lục

### Cấu trúc thư mục

```
src/main/java/com/flash/film/
├── FilmBeApplication.java ✅
├── common/
│   ├── config/
│   │   ├── security/   (SecurityConfig, JwtAuthFilter, CustomUserDetails) ✅
│   │   ├── openapi/    (OpenApiConfig, RedoclyController) ✅
│   │   ├── rabbitmq/   ✅
│   │   └── redis/      ✅
│   ├── dto/            (ApiResponse, PageResponse) ✅
│   ├── entity/         (BaseEntity) ✅
│   ├── enums/          (AppCode, UserType, ProductType, ...) ✅
│   ├── exception/      (CustomException, GlobalExceptionHandler) ✅
│   └── util/           (JwtUtil, CookieUtil, SlugUtil ❌)
└── module/
    ├── auth/           ✅
    ├── user/           ✅
    ├── product/        ✅ (public) / ❌ (admin)
    ├── category/       ✅ (public) / ❌ (admin)
    ├── media/          ✅
    ├── permission/     ✅
    ├── token/          ✅
    ├── log/            ✅
    ├── notification/   ✅
    ├── rabbitmq/       ✅
    ├── redis/          ✅
    ├── cart/           ❌
    ├── order/          ❌
    └── payment/        ❌
```

### RBAC — Phân quyền seed mặc định ✅

| Role | Quyền |
|---|---|
| ADMIN | Full `/film/**` (GET/POST/PUT/PATCH/DELETE) |
| MODERATOR | Full GET/POST/PUT/PATCH `/film/**` |
| USER | `/film/user/v1/**`, `/film/auth/v1/logout`, `/film/auth/v1/change-password`, `/film/token/v1/**` |

### Roadmap tổng hợp

| Phase | Nội dung | Status |
|---|---|---|
| Phase 1 | Auth, Security, Media, User Profile | ✅ |
| Phase 2 | DB Schema sản phẩm (6 bảng) | ✅ |
| Phase 3 | Public API catalog | ✅ |
| Phase 4 | Admin CRUD Product & Category | ❌ |
| Phase 5 | Cart, Checkout, Payment | ❌ |
| Phase 6 | Order management, Workflow xưởng in | ❌ |

### Môi trường chạy Dev

```bash
# Khởi động infrastructure
docker compose up -d

# Chạy ứng dụng
./mvnw spring-boot:run

# API docs
http://localhost:8080/redoc
http://localhost:8080/api-docs
```
