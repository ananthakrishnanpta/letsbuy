package com.letsbuy.common.exception;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends AppException {

    public AccountLockedException() {
        super(HttpStatus.LOCKED, "ACCOUNT_LOCKED", "Account is temporarily locked. Try again later.");
    }
}
