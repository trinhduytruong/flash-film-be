package com.flash.film.module.token.service;

import com.flash.film.module.token.entity.UserToken;
import jakarta.servlet.http.HttpServletRequest;

public interface UserTokenService {
    UserToken saveTokens(Long userId, String accessToken, String refreshToken, HttpServletRequest request);
    void revokeByAccessToken(String accessToken, Long userId);
    void updateAccessToken(UserToken token, String newAccessToken, String oldAccessToken);
    boolean isBlacklisted(String token);
}
