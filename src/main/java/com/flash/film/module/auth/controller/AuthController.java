package com.flash.film.module.auth.controller;

import com.flash.film.common.dto.ApiResponse;
import com.flash.film.common.enums.AppCode;
import com.flash.film.common.exception.CustomException;
import com.flash.film.common.util.CookieUtil;
import com.flash.film.module.auth.dto.ChangePasswordRequest;
import com.flash.film.module.auth.dto.LoginRequest;
import com.flash.film.module.auth.dto.LoginResponse;
import com.flash.film.module.auth.dto.RegisterRequest;
import com.flash.film.module.auth.dto.SendOtpRequest;
import com.flash.film.module.auth.service.AuthService;
import com.flash.film.module.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/film/auth/v1")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication — Register / Login / Logout / Refresh / Change Password")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/send-register-otp")
    @Operation(summary = "Send OTP to email for registration")
    public ResponseEntity<ApiResponse<Void>> sendRegisterOtp(
            @RequestBody @Valid SendOtpRequest request) {

        authService.sendRegisterOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(null, AppCode.SUCCESS, "OTP has been sent to your email"));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @RequestBody @Valid RegisterRequest request) {

        User user = authService.register(request);

        Map<String, Object> data = Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(data, AppCode.REGISTER_SUCCESS));
    }

    @PostMapping("/login")
    @Operation(summary = "Login — access token in body, refresh token in HTTP-only cookie")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        LoginResponse result = authService.login(request, httpRequest);

        if (Boolean.TRUE.equals(result.getRefreshEnabled()) && StringUtils.hasText(result.getRefreshToken())) {
            int ttlSec = (int) (authService.getRefreshTtlMs() / 1000);
            cookieUtil.createHttpOnlyCookie(httpResponse, CookieUtil.REFRESH_TOKEN_COOKIE, result.getRefreshToken(),
                    ttlSec);
        }

        return ResponseEntity.ok(ApiResponse.ok(result, AppCode.LOGIN_SUCCESS));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Logout — revoke token, clear refresh cookie")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse response) {

        Long userId = Long.parseLong(userDetails.getUsername());
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            authService.logout(userId, bearer.substring(7));
        }
        cookieUtil.clearCookie(response, CookieUtil.REFRESH_TOKEN_COOKIE);

        return ResponseEntity.ok(ApiResponse.ok(null, AppCode.LOGOUT_SUCCESS));
    }

    @PutMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change password — revokes all sessions after success")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChangePasswordRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());
        authService.changePassword(userId, request);

        return ResponseEntity.ok(ApiResponse.ok(null, AppCode.CHANGE_PASSWORD_SUCCESS));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token cookie")
    public ResponseEntity<ApiResponse<String>> refresh(HttpServletRequest request) {

        String refreshToken = cookieUtil.getCookieValue(request, CookieUtil.REFRESH_TOKEN_COOKIE)
                .orElseThrow(() -> new CustomException(
                        AppCode.REFRESH_TOKEN_INVALID,
                        HttpStatus.UNAUTHORIZED,
                        AppCode.REFRESH_TOKEN_INVALID.getMessageEn()));

        String newAccessToken = authService.refresh(refreshToken);

        return ResponseEntity.ok(ApiResponse.ok(newAccessToken, AppCode.TOKEN_REFRESHED));
    }
}
