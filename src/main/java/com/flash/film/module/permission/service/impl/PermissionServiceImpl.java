package com.flash.film.module.permission.service.impl;

import com.flash.film.module.permission.service.PermissionService;

import com.flash.film.common.enums.AppCode;
import com.flash.film.common.exception.CustomException;
import com.flash.film.module.permission.entity.PermissionApi;
import com.flash.film.module.permission.repository.PermissionApiRepository;
import com.flash.film.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * Permission service — check xem user có quyền gọi API (method + uri) không.
 * Dựa vào user_type và bảng permission_api.
 * Các public endpoint (login, api-docs, redoc) không đi qua đây.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionApiRepository permissionApiRepository;
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public void checkAccessApi(User user, String httpMethod, String requestUri) {
        List<PermissionApi> permissions = permissionApiRepository.findActiveByUserTypeAndMethod(user.getUserType(),
                httpMethod.toUpperCase());

        boolean allowed = permissions.stream()
                .anyMatch(p -> PATH_MATCHER.match(p.getUriPattern(), requestUri));

        if (!allowed) {
            log.warn("FORBIDDEN — userId={} userType={} method={} uri={}",
                    user.getId(), user.getUserType(), httpMethod, requestUri);
            throw new CustomException(AppCode.FORBIDDEN, HttpStatus.FORBIDDEN,
                    "Access denied: " + httpMethod + " " + requestUri);
        }

        log.debug("ALLOWED — userId={} userType={} method={} uri={}",
                user.getId(), user.getUserType(), httpMethod, requestUri);
    }
}
