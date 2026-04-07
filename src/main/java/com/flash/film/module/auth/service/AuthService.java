package com.flash.film.module.auth.service;

import com.flash.film.module.auth.dto.ChangePasswordRequest;
import com.flash.film.module.auth.dto.LoginRequest;
import com.flash.film.module.auth.dto.LoginResponse;
import com.flash.film.module.auth.dto.RegisterRequest;
import com.flash.film.module.auth.dto.ResetPasswordRequest;
import com.flash.film.module.auth.dto.SendOtpRequest;
import com.flash.film.module.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    void sendRegisterOtp(SendOtpRequest request);
    void sendForgotPasswordOtp(SendOtpRequest request);
    void resetPassword(ResetPasswordRequest request);
    User register(RegisterRequest request);
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);
    void changePassword(Long userId, ChangePasswordRequest request);
    void logout(Long userId, String accessToken);
    String refresh(String refreshToken);
    long getRefreshTtlMs();
}
