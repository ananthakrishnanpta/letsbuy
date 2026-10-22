package com.letsbuy.identity.web;

import com.letsbuy.common.api.ApiResponse;
import com.letsbuy.common.exception.ForbiddenException;
import com.letsbuy.common.exception.UnauthorizedException;
import com.letsbuy.identity.service.AuthService;
import com.letsbuy.identity.web.dto.AuthResponse;
import com.letsbuy.identity.web.dto.EmailRequest;
import com.letsbuy.identity.web.dto.LoginRequest;
import com.letsbuy.identity.web.dto.RegisterRequest;
import com.letsbuy.identity.web.dto.ResetPasswordRequest;
import com.letsbuy.identity.web.dto.TokenRequest;
import com.letsbuy.identity.web.dto.UserResponse;
import com.letsbuy.security.CurrentUser;
import com.letsbuy.security.RefreshCookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    static final String BROWSER_HEADER = "X-Letsbuy-Request";

    private final AuthService authService;
    private final RefreshCookie refreshCookie;

    public AuthController(AuthService authService, RefreshCookie refreshCookie) {
        this.authService = authService;
        this.refreshCookie = refreshCookie;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http, HttpServletResponse response) {
        AuthService.IssuedSession session = authService.register(request);
        writeRefresh(response, session);
        return ApiResponse.of(session.toResponse());
    }

    @PostMapping("/login")
    ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http, HttpServletResponse response) {
        AuthService.IssuedSession session = authService.login(
                request.email(),
                request.password(),
                http.getHeader("User-Agent"),
                http.getRemoteAddr()
        );
        writeRefresh(response, session);
        return ApiResponse.of(session.toResponse());
    }

    @PostMapping("/refresh")
    ApiResponse<AuthResponse> refresh(HttpServletRequest http, HttpServletResponse response) {
        requireBrowserHeader(http);
        String raw = refreshCookie.read(http);
        if (raw == null) {
            throw new UnauthorizedException("Session expired.");
        }
        AuthService.IssuedSession session = authService.refresh(raw, http.getHeader("User-Agent"), http.getRemoteAddr());
        writeRefresh(response, session);
        return ApiResponse.of(session.toResponse());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(HttpServletRequest http, HttpServletResponse response) {
        requireBrowserHeader(http);
        authService.logout(refreshCookie.read(http));
        refreshCookie.clear(response);
    }

    @GetMapping("/me")
    ApiResponse<UserResponse> me() {
        return ApiResponse.of(authService.me(CurrentUser.id()));
    }

    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void verifyEmail(@Valid @RequestBody TokenRequest request) {
        authService.verifyEmail(request.token());
    }

    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void resend(@Valid @RequestBody EmailRequest request) {
        authService.resendVerification(request.email());
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void forgot(@Valid @RequestBody EmailRequest request) {
        authService.forgotPassword(request.email());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reset(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.password());
    }

    private void writeRefresh(HttpServletResponse response, AuthService.IssuedSession session) {
        if (session.refreshToken() != null) {
            refreshCookie.write(response, session.refreshToken());
        }
    }

    private static void requireBrowserHeader(HttpServletRequest request) {
        if (!"1".equals(request.getHeader(BROWSER_HEADER))) {
            throw new ForbiddenException("Missing request header.");
        }
    }
}
