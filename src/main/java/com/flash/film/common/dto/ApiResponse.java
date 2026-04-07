package com.flash.film.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flash.film.common.enums.AppCode;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;

    private final int httpCode;

    private final Integer code;

    private final String message;

    private final T data;

    private final String timestamp;

    // ── Factory methods ────────────────────────────────────────────────────

    public static <T> ApiResponse<T> ok(T data, AppCode appCode, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .httpCode(HttpStatus.OK.value())
                .code(appCode.getCode())
                .message(message)
                .data(data)
                .timestamp(now())
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, AppCode appCode) {
        return ok(data, appCode, appCode.getMessageEn());
    }

    public static <T> ApiResponse<T> created(T data, AppCode appCode, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .httpCode(HttpStatus.CREATED.value())
                .code(appCode.getCode())
                .message(message)
                .data(data)
                .timestamp(now())
                .build();
    }

    public static <T> ApiResponse<T> created(T data, AppCode appCode) {
        return created(data, appCode, appCode.getMessageEn());
    }

    public static ApiResponse<Void> error(AppCode appCode, String message, HttpStatus status) {
        return ApiResponse.<Void>builder()
                .success(false)
                .httpCode(status.value())
                .code(appCode.getCode())
                .message(message)
                .timestamp(now())
                .build();
    }

    public static ApiResponse<Void> error(AppCode appCode, String message, int httpCode) {
        return ApiResponse.<Void>builder()
                .success(false)
                .httpCode(httpCode)
                .code(appCode.getCode())
                .message(message)
                .timestamp(now())
                .build();
    }

    private static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
