package com.banking_system.bank_mang.t.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
/**
 * Custom exception for when an operation is attempted on an account that is not yet approved.
 * Maps to HTTP 403 Forbidden or 400 Bad Request depending on interpretation.
 */
@ResponseStatus(HttpStatus.FORBIDDEN) // Or HttpStatus.BAD_REQUEST
public class AccountNotApprovedException extends RuntimeException {
    public AccountNotApprovedException(String message) {
        super(message);
    }
}
