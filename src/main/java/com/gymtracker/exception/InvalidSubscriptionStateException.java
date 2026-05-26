package com.gymtracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InvalidSubscriptionStateException extends RuntimeException {
    public InvalidSubscriptionStateException(String message) {
        super(message);
    }
}
