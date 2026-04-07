package com.flash.film.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AppCode {

    // ── Success ──────────────────────────────────────────────
    SUCCESS(1000, "Success", "Thành công"),
    LOGIN_SUCCESS(1001, "Login successful", "Đăng nhập thành công"),
    LOGOUT_SUCCESS(1002, "Logout successful", "Đăng xuất thành công"),
    TOKEN_REFRESHED(1003, "Token refreshed", "Làm mới token thành công"),
    REGISTER_SUCCESS(1004, "Registration successful", "Đăng ký thành công"),
    CHANGE_PASSWORD_SUCCESS(1005, "Password changed successfully", "Đổi mật khẩu thành công"),

    // ── Client errors ────────────────────────────────────────
    VALIDATION_ERROR(4000, "Validation error", "Lỗi xác thực dữ liệu"),
    UNAUTHORIZED(4001, "Unauthorized", "Chưa xác thực"),
    TOKEN_EXPIRED(4002, "Token expired", "Token đã hết hạn"),
    TOKEN_INVALID(4003, "Token invalid", "Token không hợp lệ"),
    REFRESH_TOKEN_INVALID(4004, "Refresh token invalid", "Refresh token không hợp lệ"),
    REFRESH_DISABLED(4005, "Refresh token feature is disabled", "Tính năng refresh token đã tắt"),
    FORBIDDEN(4010, "Access denied", "Không có quyền truy cập"),
    NOT_FOUND(4040, "Resource not found", "Không tìm thấy tài nguyên"),

    // ── Auth errors ──────────────────────────────────────────
    INVALID_CREDENTIALS(4011, "Invalid username or password", "Tên đăng nhập hoặc mật khẩu không đúng"),
    USER_NOT_FOUND(4012, "User not found", "Không tìm thấy người dùng"),
    REFRESH_TOKEN_REVOKED(4013, "Refresh token has been revoked", "Refresh token đã bị thu hồi"),
    REFRESH_TOKEN_NOT_FOUND(4014, "Refresh token not found", "Không tìm thấy refresh token"),
    USERNAME_EXISTS(4015, "Username already exists", "Tên đăng nhập đã tồn tại"),
    EMAIL_EXISTS(4016, "Email already exists", "Email đã tồn tại"),
    WRONG_PASSWORD(4017, "Current password is incorrect", "Mật khẩu hiện tại không đúng"),

    // ── Server errors ────────────────────────────────────────
    INTERNAL_ERROR(5000, "Internal server error", "Lỗi hệ thống");

    private final int code;
    private final String messageEn;
    private final String messageVi;

    AppCode(int code, String messageEn, String messageVi) {
        this.code = code;
        this.messageEn = messageEn;
        this.messageVi = messageVi;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    public String getMessageEn() {
        return messageEn;
    }

    public String getMessageVi() {
        return messageVi;
    }

    /**
     * Lấy message theo ngôn ngữ.
     * @param lang "vi" cho tiếng Việt, mặc định trả tiếng Anh
     */
    public String getMessage(String lang) {
        return "vi".equalsIgnoreCase(lang) ? messageVi : messageEn;
    }
}
