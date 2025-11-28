package com.difbriy.web.exception.custom;

public class AuthenticationEntryPointException extends RuntimeException {
    public AuthenticationEntryPointException(String message) {
        super(message);
    }
}
