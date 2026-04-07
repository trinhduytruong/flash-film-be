package com.flash.film.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    /**
     * Tạo HTTP-only, Secure, SameSite=Strict cookie.
     *
     * @param name      tên cookie
     * @param value     giá trị
     * @param maxAgeSec TTL (giây)
     */
    public void createHttpOnlyCookie(HttpServletResponse response, String name, String value, int maxAgeSec) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Chuyển true khi deploy HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSec);
        response.addCookie(cookie);
        response.addHeader("Set-Cookie",
                String.format("%s=%s; Max-Age=%d; Path=/; HttpOnly; SameSite=Strict",
                        name, value, maxAgeSec));
    }

    public void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public Optional<String> getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null)
            return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
