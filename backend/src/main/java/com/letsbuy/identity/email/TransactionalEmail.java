package com.letsbuy.identity.email;

public interface TransactionalEmail {

    void sendVerification(String email, String rawToken);

    void sendPasswordReset(String email, String rawToken);
}
