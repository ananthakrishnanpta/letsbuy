package com.letsbuy.support;

import com.letsbuy.identity.email.TransactionalEmail;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestEmailSender implements TransactionalEmail {

    public final CopyOnWriteArrayList<String> verificationTokens = new CopyOnWriteArrayList<>();
    public final CopyOnWriteArrayList<String> resetTokens = new CopyOnWriteArrayList<>();

    @Override
    public void sendVerification(String email, String rawToken) {
        verificationTokens.add(rawToken);
    }

    @Override
    public void sendPasswordReset(String email, String rawToken) {
        resetTokens.add(rawToken);
    }
}
