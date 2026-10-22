package com.letsbuy.security;

import com.letsbuy.config.AppProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshCookie {

    public static final String NAME = "letsbuy_refresh";
    public static final String PATH = "/api/v1/auth";

    private final AppProperties properties;

    public RefreshCookie(AppProperties properties) {
        this.properties = properties;
    }

    public void write(HttpServletResponse response, String rawToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(rawToken, properties.jwt().refreshTokenTtl()).toString());
    }

    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString());
    }

    public String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (NAME.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(properties.auth().cookieSecure())
                .sameSite("Lax")
                .path(PATH)
                .maxAge(maxAge)
                .build();
    }
}
