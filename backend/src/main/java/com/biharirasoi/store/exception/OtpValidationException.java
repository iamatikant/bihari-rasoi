package com.biharirasoi.store.exception;

public class OtpValidationException extends RuntimeException {
    public OtpValidationException(String message) {
        super(message);
    }

    public OtpValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
