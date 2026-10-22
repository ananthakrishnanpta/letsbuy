package com.letsbuy.identity.service;

import com.letsbuy.common.exception.AccountLockedException;
import com.letsbuy.common.exception.ConflictException;
import com.letsbuy.common.exception.EmailNotVerifiedException;
import com.letsbuy.common.exception.ForbiddenException;
import com.letsbuy.common.exception.UnauthorizedException;
import com.letsbuy.config.AppProperties;
import com.letsbuy.identity.domain.EmailVerificationToken;
import com.letsbuy.identity.domain.PasswordResetToken;
import com.letsbuy.identity.domain.RefreshToken;
import com.letsbuy.identity.domain.Role;
import com.letsbuy.identity.domain.RoleName;
import com.letsbuy.identity.domain.User;
import com.letsbuy.identity.domain.UserStatus;
import com.letsbuy.identity.email.TransactionalEmail;
import com.letsbuy.identity.persistence.EmailVerificationTokenRepository;
import com.letsbuy.identity.persistence.PasswordResetTokenRepository;
import com.letsbuy.identity.persistence.RefreshTokenRepository;
import com.letsbuy.identity.persistence.RoleRepository;
import com.letsbuy.identity.persistence.UserRepository;
import com.letsbuy.identity.web.dto.AuthResponse;
import com.letsbuy.identity.web.dto.RegisterRequest;
import com.letsbuy.identity.web.dto.UserResponse;
import com.letsbuy.security.AccessTokenService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class AuthService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final RefreshTokenRepository refreshTokens;
    private final EmailVerificationTokenRepository verificationTokens;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenService accessTokens;
    private final TransactionalEmail email;
    private final AppProperties properties;
    private final TransactionTemplate tx;

    public AuthService(
            UserRepository users,
            RoleRepository roles,
            RefreshTokenRepository refreshTokens,
            EmailVerificationTokenRepository verificationTokens,
            PasswordResetTokenRepository resetTokens,
            PasswordEncoder passwordEncoder,
            AccessTokenService accessTokens,
            TransactionalEmail email,
            AppProperties properties,
            PlatformTransactionManager transactionManager
    ) {
        this.users = users;
        this.roles = roles;
        this.refreshTokens = refreshTokens;
        this.verificationTokens = verificationTokens;
        this.resetTokens = resetTokens;
        this.passwordEncoder = passwordEncoder;
        this.accessTokens = accessTokens;
        this.email = email;
        this.properties = properties;
        this.tx = new TransactionTemplate(transactionManager);
    }

    @Transactional
    public IssuedSession register(RegisterRequest request) {
        String emailAddress = normalize(request.email());
        if (users.existsByEmail(emailAddress)) {
            throw new ConflictException("An account with this email already exists.");
        }
        User user = new User();
        user.setEmail(emailAddress);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(blankToNull(request.phone()));
        user.getRoles().add(customerRole());
        if (properties.auth().requireEmailVerification()) {
            user.setStatus(UserStatus.PENDING_VERIFICATION);
            users.save(user);
            sendVerification(user);
            return IssuedSession.pending(UserResponse.from(user));
        }
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerifiedAt(Instant.now());
        users.save(user);
        return issue(user, null, null, null);
    }

    public IssuedSession login(String emailAddress, String password, String userAgent, String ip) {
        User user = tx.execute(status -> users.findByEmail(normalize(emailAddress)).orElse(null));
        if (user == null) {
            throw invalidCredentials();
        }
        Instant now = Instant.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new AccountLockedException();
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            tx.executeWithoutResult(status -> recordFailure(user.getId()));
            throw invalidCredentials();
        }
        return tx.execute(status -> completeLogin(user.getId(), userAgent, ip));
    }

    public IssuedSession refresh(String rawToken, String userAgent, String ip) {
        try {
            return tx.execute(status -> rotate(rawToken, userAgent, ip));
        } catch (TokenReuseException reuse) {
            tx.executeWithoutResult(status -> refreshTokens.revokeFamily(reuse.familyId, Instant.now()));
            throw new UnauthorizedException("Session revoked.");
        } catch (InactiveSessionException inactive) {
            tx.executeWithoutResult(status -> refreshTokens.revokeAllForUser(inactive.userId, Instant.now()));
            throw new UnauthorizedException("Session expired.");
        }
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokens.findByTokenHash(OpaqueTokens.sha256(rawToken)).ifPresent(token -> {
            Instant now = Instant.now();
            refreshTokens.revokeFamily(token.getFamilyId(), now);
        });
    }

    @Transactional(readOnly = true)
    public UserResponse me(UUID userId) {
        return users.findWithRolesById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new UnauthorizedException("Authentication required."));
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        EmailVerificationToken token = verificationTokens.findByTokenHash(OpaqueTokens.sha256(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Verification link is invalid."));
        Instant now = Instant.now();
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(now)) {
            throw new UnauthorizedException("Verification link is invalid or expired.");
        }
        User user = users.findWithRolesById(token.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Verification link is invalid."));
        token.setUsedAt(now);
        user.setEmailVerifiedAt(now);
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            user.setStatus(UserStatus.ACTIVE);
        }
    }

    @Transactional
    public void resendVerification(String emailAddress) {
        users.findByEmail(normalize(emailAddress)).ifPresent(user -> {
            if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
                sendVerification(user);
            }
        });
    }

    @Transactional
    public void forgotPassword(String emailAddress) {
        users.findByEmail(normalize(emailAddress)).ifPresent(user -> {
            if (user.getStatus() == UserStatus.DISABLED) {
                return;
            }
            PasswordResetToken token = new PasswordResetToken();
            String raw = OpaqueTokens.newRaw();
            token.setUserId(user.getId());
            token.setTokenHash(OpaqueTokens.sha256(raw));
            token.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
            resetTokens.save(token);
            email.sendPasswordReset(user.getEmail(), raw);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = resetTokens.findByTokenHash(OpaqueTokens.sha256(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Reset link is invalid."));
        Instant now = Instant.now();
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(now)) {
            throw new UnauthorizedException("Reset link is invalid or expired.");
        }
        User user = users.findWithRolesById(token.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Reset link is invalid."));
        token.setUsedAt(now);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }
        refreshTokens.revokeAllForUser(user.getId(), now);
    }

    private void sendVerification(User user) {
        EmailVerificationToken token = new EmailVerificationToken();
        String raw = OpaqueTokens.newRaw();
        token.setUserId(user.getId());
        token.setTokenHash(OpaqueTokens.sha256(raw));
        token.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        verificationTokens.save(token);
        email.sendVerification(user.getEmail(), raw);
    }

    private IssuedSession completeLogin(UUID userId, String userAgent, String ip) {
        User user = users.findWithRolesById(userId).orElseThrow(this::invalidCredentials);
        Instant now = Instant.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new AccountLockedException();
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new ForbiddenException("This account is disabled.");
        }
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new EmailNotVerifiedException();
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("This account cannot sign in.");
        }
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        return issue(user, null, userAgent, ip);
    }

    private IssuedSession rotate(String rawToken, String userAgent, String ip) {
        RefreshToken current = refreshTokens.findByTokenHash(OpaqueTokens.sha256(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Session expired."));
        Instant now = Instant.now();
        if (current.getRevokedAt() != null) {
            throw new TokenReuseException(current.getFamilyId());
        }
        if (current.getExpiresAt().isBefore(now)) {
            throw new UnauthorizedException("Session expired.");
        }
        User user = users.findWithRolesById(current.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Session expired."));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InactiveSessionException(user.getId());
        }
        current.setRevokedAt(now);
        IssuedSession next = issue(user, current.getFamilyId(), userAgent, ip);
        current.setReplacedBy(next.refreshTokenId());
        return next;
    }

    private void recordFailure(UUID userId) {
        User user = users.findById(userId).orElseThrow(this::invalidCredentials);
        Instant now = Instant.now();
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= properties.auth().maxFailedLogins()) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockedUntil(now.plus(properties.auth().lockDuration()));
            user.setFailedLoginAttempts(0);
        }
    }

    private IssuedSession issue(User user, UUID familyId, String userAgent, String ip) {
        String rawRefresh = OpaqueTokens.newRaw();
        RefreshToken refresh = new RefreshToken();
        refresh.setUserId(user.getId());
        refresh.setTokenHash(OpaqueTokens.sha256(rawRefresh));
        refresh.setFamilyId(familyId == null ? UUID.randomUUID() : familyId);
        refresh.setExpiresAt(Instant.now().plus(properties.jwt().refreshTokenTtl()));
        refresh.setUserAgent(truncate(userAgent, 512));
        refresh.setIpAddress(truncate(ip, 64));
        refreshTokens.save(refresh);
        var roleNames = user.getRoles().stream().map(role -> role.getName().name()).sorted().toList();
        String access = accessTokens.issue(user.getId(), user.getEmail(), roleNames);
        return new IssuedSession(access, accessTokens.expiresInSeconds(), rawRefresh, refresh.getId(), UserResponse.from(user));
    }

    private Role customerRole() {
        return roles.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role is not seeded"));
    }

    private UnauthorizedException invalidCredentials() {
        return new UnauthorizedException("Invalid email or password.");
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    public record IssuedSession(String accessToken, long expiresInSeconds, String refreshToken, UUID refreshTokenId, UserResponse user) {

        static IssuedSession pending(UserResponse user) {
            return new IssuedSession(null, 0, null, null, user);
        }

        public AuthResponse toResponse() {
            return new AuthResponse(accessToken, expiresInSeconds, user);
        }
    }

    private static final class TokenReuseException extends RuntimeException {
        private final UUID familyId;

        private TokenReuseException(UUID familyId) {
            this.familyId = familyId;
        }
    }

    private static final class InactiveSessionException extends RuntimeException {
        private final UUID userId;

        private InactiveSessionException(UUID userId) {
            this.userId = userId;
        }
    }
}
