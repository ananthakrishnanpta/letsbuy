package com.letsbuy.identity.email;

import com.letsbuy.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class LoggingEmailSender implements TransactionalEmail {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    private final AppProperties properties;

    public LoggingEmailSender(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public void sendVerification(String email, String rawToken) {
        if (properties.auth().logEmailTokens()) {
            log.info("Verification link for {}: {}/verify-email?token={}", email, properties.auth().publicBaseUrl(), rawToken);
        } else {
            log.info("Queued verification email for {}", email);
        }
    }

    @Override
    public void sendPasswordReset(String email, String rawToken) {
        if (properties.auth().logEmailTokens()) {
            log.info("Password reset link for {}: {}/reset-password?token={}", email, properties.auth().publicBaseUrl(), rawToken);
        } else {
            log.info("Queued password reset email for {}", email);
        }
    }
}
