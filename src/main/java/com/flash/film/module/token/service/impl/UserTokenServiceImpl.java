package com.flash.film.module.token.service.impl;

import com.flash.film.module.token.service.UserTokenService;

import com.flash.film.common.config.AppProperties;
import com.flash.film.module.token.entity.UserToken;
import com.flash.film.module.token.repository.UserTokenRepository;
import com.flash.film.module.redis.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTokenServiceImpl implements UserTokenService {

    private final UserTokenRepository userTokenRepository;
    private final RedisService redisService;
    private final AppProperties appProperties;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Transactional
    public UserToken saveTokens(Long userId, String accessToken, String refreshToken,
                                HttpServletRequest request) {
        long accessTtlSec  = appProperties.getJwt().getAccessTokenExpirationMs() / 1000;
        long refreshTtlSec = appProperties.getJwt().getRefreshTokenExpirationMs() / 1000;

        UserToken token = new UserToken();
        token.setUserId(userId);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setDeviceIp(resolveIp(request));
        token.setUserAgent(request.getHeader("User-Agent"));
        token.setAccessExpiresAt(new Timestamp(System.currentTimeMillis() + accessTtlSec * 1000));
        token.setRefreshExpiresAt(refreshToken != null ? new Timestamp(System.currentTimeMillis() + refreshTtlSec * 1000) : null);
        token.setIsRevoked(false);
        token.setCreatedBy(userId.toString());
        return userTokenRepository.save(token);
    }

    @Transactional
    public void revokeByAccessToken(String accessToken, Long userId) {
        userTokenRepository.findByAccessTokenAndIsRevokedFalse(accessToken).ifPresent(token -> {
            token.setIsRevoked(true);
            token.setRevokedAt(new Timestamp(System.currentTimeMillis()));
            token.setRevokeReason("LOGOUT");
            token.setUpdatedBy(userId.toString());
            userTokenRepository.save(token);

            // Blacklist in Redis
            long accessTtlSec  = appProperties.getJwt().getAccessTokenExpirationMs() / 1000;
            long refreshTtlSec = appProperties.getJwt().getRefreshTokenExpirationMs() / 1000;
            redisService.setWithoutLog(BLACKLIST_PREFIX + accessToken, "1", accessTtlSec);
            if (token.getRefreshToken() != null) {
                redisService.setWithoutLog(BLACKLIST_PREFIX + token.getRefreshToken(), "1", refreshTtlSec);
            }
        });
    }

    @Transactional
    public void updateAccessToken(UserToken token, String newAccessToken, String oldAccessToken) {
        long accessTtlSec = appProperties.getJwt().getAccessTokenExpirationMs() / 1000;
        // Blacklist old access token
        redisService.setWithoutLog(BLACKLIST_PREFIX + oldAccessToken, "1", accessTtlSec);
        token.setAccessToken(newAccessToken);
        token.setAccessExpiresAt(new Timestamp(System.currentTimeMillis() + accessTtlSec * 1000));
        userTokenRepository.save(token);
    }

    public boolean isBlacklisted(String token) {
        return redisService.exists(BLACKLIST_PREFIX + token);
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
