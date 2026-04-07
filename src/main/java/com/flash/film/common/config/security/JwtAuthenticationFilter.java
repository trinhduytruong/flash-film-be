package com.flash.film.common.config.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flash.film.common.enums.AppCode;
import com.flash.film.common.exception.CustomException;
import com.flash.film.common.util.JwtUtil;
import com.flash.film.module.log.entity.AccessLog;
import com.flash.film.module.log.repository.AccessLogRepository;
import com.flash.film.module.permission.service.PermissionService;
import com.flash.film.module.user.entity.User;
import com.flash.film.module.user.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final PermissionService permissionService;
    private final AccessLogRepository accessLogRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String[] SENSITIVE_FIELDS = {
            "password", "old_password", "new_password", "confirm_password",
            "pwd", "secret", "token", "jwt_secret"
    };

    private static final String[] PUBLIC_PATHS = {
            "/film/auth/v1/register",
            "/film/auth/v1/login",
            "/film/auth/v1/refresh",
            "/api-docs",
            "/redoc",
            "/v3/api-docs",
            "/actuator/health"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
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
        AccessLog accessLog = buildInitialLog(request);
        long startTime = System.currentTimeMillis();

        try {
            String jwt = extractJwt(request);

            if (StringUtils.hasText(jwt)) {
                // decode payload to get userId (no verification yet)
                Long userId = jwtUtil.extractUserIdWithoutVerification(jwt);

                // load user + jwtSecret from DB
                User user = userDetailsService.loadRawUserById(userId);

                // validate JWT with per-user secret
                if (!jwtUtil.validateToken(jwt, user.getJwtSecret())) {
                    sendError(response, accessLog, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
                    return;
                }

                // set SecurityContext
                UserDetails userDetails = userDetailsService.loadUserById(userId);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                        userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                accessLog.setUserId(userId);
                accessLog.setUserType(user.getUserType().name());

                // check permission BEFORE processing
                permissionService.checkAccessApi(user, request.getMethod(), request.getRequestURI());
            }

            filterChain.doFilter(wrappedRequest, response);

        } catch (CustomException ex) {
            log.warn("CustomException in filter: {}", ex.getMessage());
            accessLog.setException(ex.getMessage());
            writeJsonError(response, ex.getAppCode(), ex.getMessage(), ex.getHttpStatus().value());
        } catch (Exception ex) {
            log.error("Filter error: {}", ex.getMessage(), ex);
            accessLog.setException(ex.getMessage());
            sendError(response, accessLog, HttpStatus.UNAUTHORIZED, "Unauthorized");
        } finally {
            appendBodyToLog(wrappedRequest, accessLog);
            accessLog.setDurationMs(System.currentTimeMillis() - startTime);
            saveLog(accessLog);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String extractJwt(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private AccessLog buildInitialLog(HttpServletRequest request) {
        AccessLog log = new AccessLog();
        log.setUri(request.getRequestURI());
        log.setHttpMethod(request.getMethod());
        log.setFromIp(request.getHeader("X-Forwarded-For") != null
                ? request.getHeader("X-Forwarded-For")
                : request.getRemoteAddr());
        log.setDeviceId(request.getHeader("device-id"));
        log.setParams(request.getParameterMap().toString());
        log.setRequestAt(new java.sql.Timestamp(System.currentTimeMillis()));
        return log;
    }

    private void appendBodyToLog(ContentCachingRequestWrapper request, AccessLog accessLog) {
        try {
            byte[] bodyBytes = request.getContentAsByteArray();
            if (bodyBytes.length == 0)
                return;
            String body = new String(bodyBytes, StandardCharsets.UTF_8);
            JsonNode node = MAPPER.readTree(body);
            if (node instanceof ObjectNode obj) {
                for (String field : SENSITIVE_FIELDS) {
                    if (obj.has(field))
                        obj.put(field, "***");
                }
            }
            accessLog.setRequestBody(MAPPER.writeValueAsString(node));
        } catch (Exception e) {
            log.debug("Could not parse request body: {}", e.getMessage());
        }
    }

    private void sendError(HttpServletResponse response, AccessLog accessLog,
            HttpStatus status, String message) throws IOException {
        accessLog.setException(message);
        accessLog.setHttpStatus(status.value());
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

    private void saveLog(AccessLog accessLog) {
        try {
            accessLogRepository.save(accessLog);
        } catch (Exception e) {
            log.error("Failed to save access log: {}", e.getMessage());
        }
    }
}
