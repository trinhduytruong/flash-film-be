package com.flash.film.module.auth.service;

import com.flash.film.common.config.AppProperties;
import com.flash.film.common.enums.AppCode;
import com.flash.film.common.exception.CustomException;
import com.flash.film.common.util.JwtUtil;
import com.flash.film.module.auth.dto.LoginRequest;
import com.flash.film.module.auth.dto.LoginResponse;
import com.flash.film.module.auth.dto.RegisterRequest;
import com.flash.film.module.auth.dto.ChangePasswordRequest;
import com.flash.film.common.enums.UserType;
import com.flash.film.module.token.entity.UserToken;
import com.flash.film.module.token.repository.UserTokenRepository;
import com.flash.film.module.token.service.UserTokenService;
import com.flash.film.module.user.entity.User;
import com.flash.film.module.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final UserTokenRepository userTokenRepository;
        private final UserTokenService userTokenService;
        private final JwtUtil jwtUtil;
        private final PasswordEncoder passwordEncoder;
        private final AppProperties appProperties;

        public User register(RegisterRequest request) {
                if (userRepository.existsByUsername(request.getUsername())) {
                        throw new CustomException(AppCode.USERNAME_EXISTS, HttpStatus.CONFLICT,
                                        AppCode.USERNAME_EXISTS.getMessageEn());
                }
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new CustomException(AppCode.EMAIL_EXISTS, HttpStatus.CONFLICT,
                                        AppCode.EMAIL_EXISTS.getMessageEn());
                }

                User user = new User();
                user.setUsername(request.getUsername());
                user.setEmail(request.getEmail());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setFullName(request.getFullName());
                user.setUserType(UserType.USER);
                user.setJwtSecret(UUID.randomUUID().toString());
                user.setCreatedBy(request.getUsername());

                User saved = userRepository.save(user);
                log.info("REGISTER — userId={} username={}", saved.getId(), saved.getUsername());
                return saved;
        }

        public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
                User user = userRepository.findActiveByUsername(request.getUsername())
                                .orElseThrow(() -> new CustomException(
                                                AppCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED,
                                                "Invalid username or password"));

                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        throw new CustomException(
                                        AppCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid username or password");
                }

                boolean refreshEnabled = appProperties.getJwt().isRefreshEnabled();

                String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getJwtSecret());
                String refreshToken = refreshEnabled
                                ? jwtUtil.generateRefreshToken(user.getId(), user.getJwtSecret())
                                : null;

                userTokenService.saveTokens(user.getId(), accessToken, refreshToken, httpRequest);

                user.setLastLogin(new java.sql.Timestamp(System.currentTimeMillis()));
                userRepository.save(user);

                log.info("LOGIN — userId={} username={}", user.getId(), user.getUsername());

                return LoginResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .refreshEnabled(refreshEnabled)
                                .userType(user.getUserType().name())
                                .build();
        }


        public void changePassword(Long userId, ChangePasswordRequest request) {
                User user = userRepository.findActiveById(userId)
                                .orElseThrow(() -> new CustomException(
                                                AppCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND,
                                                AppCode.USER_NOT_FOUND.getMessageEn()));

                if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                        throw new CustomException(AppCode.WRONG_PASSWORD, HttpStatus.BAD_REQUEST,
                                        AppCode.WRONG_PASSWORD.getMessageEn());
                }

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                user.setJwtSecret(UUID.randomUUID().toString());
                user.setUpdatedBy(userId.toString());
                userRepository.save(user);

                userTokenRepository.revokeAllByUserId(userId,
                                new java.sql.Timestamp(System.currentTimeMillis()), "CHANGE_PASSWORD");

                log.info("CHANGE_PASSWORD — userId={}", userId);
        }

        public void logout(Long userId, String accessToken) {
                userTokenService.revokeByAccessToken(accessToken, userId);
                log.info("LOGOUT — userId={}", userId);
        }


        public String refresh(String refreshToken) {
                if (!appProperties.getJwt().isRefreshEnabled()) {
                        throw new CustomException(AppCode.REFRESH_DISABLED, HttpStatus.BAD_REQUEST,
                                        "Refresh token feature is disabled");
                }

                Long userId = jwtUtil.extractUserIdWithoutVerification(refreshToken);
                User user = userRepository.findActiveById(userId)
                                .orElseThrow(() -> new CustomException(
                                                AppCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "User not found"));

                if (!jwtUtil.validateToken(refreshToken, user.getJwtSecret())) {
                        throw new CustomException(AppCode.REFRESH_TOKEN_INVALID, HttpStatus.UNAUTHORIZED,
                                        "Refresh token is invalid or expired");
                }

                if (userTokenService.isBlacklisted(refreshToken)) {
                        throw new CustomException(AppCode.REFRESH_TOKEN_INVALID, HttpStatus.UNAUTHORIZED,
                                        "Refresh token has been revoked");
                }

                UserToken tokenRecord = userTokenRepository
                                .findByRefreshTokenAndIsRevokedFalse(refreshToken)
                                .orElseThrow(() -> new CustomException(
                                                AppCode.REFRESH_TOKEN_INVALID, HttpStatus.UNAUTHORIZED,
                                                "Refresh token not found"));

                String oldAccessToken = tokenRecord.getAccessToken();
                String newAccessToken = jwtUtil.generateAccessToken(userId, user.getJwtSecret());

                userTokenService.updateAccessToken(tokenRecord, newAccessToken, oldAccessToken);

                log.info("TOKEN_REFRESHED — userId={}", userId);
                return newAccessToken;
        }

        public long getRefreshTtlMs() {
                return appProperties.getJwt().getRefreshTokenExpirationMs();
        }
}
