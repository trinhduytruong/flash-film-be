package com.flash.film.common.config.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flash.film.common.enums.AppCode;
import com.flash.film.common.exception.CustomException;
import com.flash.film.module.log.service.AccessLoggerService;
import com.flash.film.common.util.KeycloakUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.flash.film.module.user.entity.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class KeycloakAuthFilter extends OncePerRequestFilter {

    private final AccessLoggerService accessLoggerService;
    private final KeycloakUserSyncService keycloakUserSyncService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String[] SENSITIVE_FIELDS = {
            "password", "old_password", "new_password", "confirm_password",
            "pwd", "secret", "token", "jwt_secret"
    };

    private static final String[] PUBLIC_PATHS = {
            "/film/public",
            "/api-docs",
            "/redoc",
            "/v3/api-docs",
            "/actuator/health"
    };

    private boolean isPublicPath(String uri) {
        for (String path : PUBLIC_PATHS) {
            if (uri.equals(path) || uri.startsWith(path + "/")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request,
                request.getContentLength() > 0 ? request.getContentLength() : 1024 * 1024);
        Map<String, Object> accessLog = buildInitialLog(request);
        long startTime = System.currentTimeMillis();

        try {
            boolean isPublic = isPublicPath(request.getRequestURI());

            if (!isPublic) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                    // Sync user JIT và lấy local ID
                    User localUser = keycloakUserSyncService.syncUser(jwt);
                    
                    // Lưu localUserId vào request attribute để Controller lấy
                    request.setAttribute("localUserId", localUser.getId());

                    // Cập nhật thông tin log
                    accessLog.put("keycloakId", KeycloakUtil.getKeycloakId(jwt));
                    accessLog.put("userId", localUser.getId());
                    accessLog.put("email", KeycloakUtil.getEmail(jwt));
                    accessLog.put("username", KeycloakUtil.getUsername(jwt));
                }
            }

            filterChain.doFilter(wrappedRequest, response);

        } catch (CustomException ex) {
            log.warn("CustomException in filter: {}", ex.getMessage());
            accessLog.put("exception", ex.getMessage());
            writeJsonError(response, ex.getAppCode(), ex.getMessage(), ex.getHttpStatus().value());
        } catch (Exception ex) {
            log.error("Filter error: {}", ex.getMessage(), ex);
            accessLog.put("exception", ex.getMessage());
            sendError(response, accessLog, HttpStatus.UNAUTHORIZED, "Unauthorized");
        } finally {
            appendBodyToLog(wrappedRequest, accessLog);
            accessLog.put("durationMs", System.currentTimeMillis() - startTime);
            saveLog(accessLog);
        }
    }

    private Map<String, Object> buildInitialLog(HttpServletRequest request) {
        Map<String, Object> log = new HashMap<>();
        log.put("uri", request.getRequestURI());
        log.put("httpMethod", request.getMethod());
        log.put("fromIp", request.getHeader("X-Forwarded-For") != null
                ? request.getHeader("X-Forwarded-For")
                : request.getRemoteAddr());
        log.put("deviceId", request.getHeader("device-id"));
        log.put("params", request.getParameterMap().toString());
        log.put("requestAt", new java.util.Date().toString());
        return log;
    }

    private void appendBodyToLog(ContentCachingRequestWrapper request, Map<String, Object> accessLog) {
        try {
            byte[] bodyBytes = request.getContentAsByteArray();
            if (bodyBytes.length == 0)
                return;
            String body = new String(bodyBytes, StandardCharsets.UTF_8);
            JsonNode node = MAPPER.readTree(body);
            if (node instanceof ObjectNode obj) {
                if (obj.has("username") && !accessLog.containsKey("username")) {
                    accessLog.put("username", obj.get("username").asText());
                }
                for (String field : SENSITIVE_FIELDS) {
                    if (obj.has(field))
                        obj.put(field, "***");
                }
            }
            accessLog.put("requestBody", MAPPER.writeValueAsString(node));
        } catch (Exception e) {
            log.debug("Could not parse request body: {}", e.getMessage());
        }
    }

    private void sendError(HttpServletResponse response, Map<String, Object> accessLog,
            HttpStatus status, String message) throws IOException {
        accessLog.put("exception", message);
        accessLog.put("httpStatus", status.value());
        writeJsonError(response, AppCode.UNAUTHORIZED, message, status.value());
    }

    private void writeJsonError(HttpServletResponse response, AppCode appCode,
            String message, int httpCode) throws IOException {
        response.setStatus(httpCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("http_code", httpCode);
        body.put("code", appCode.getCode());
        body.put("message", message);
        response.getWriter().write(MAPPER.writeValueAsString(body));
    }

    private void saveLog(Map<String, Object> accessLog) {
        try {
            accessLoggerService.logAccessAsync(accessLog);
        } catch (Exception e) {
            log.error("Failed to save access log: {}", e.getMessage());
        }
    }
}
