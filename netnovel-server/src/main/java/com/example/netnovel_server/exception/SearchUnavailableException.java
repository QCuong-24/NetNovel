package com.example.netnovel_server.exception;

import org.springframework.http.HttpStatus;

public class SearchUnavailableException extends ApiException {

    public SearchUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public SearchUnavailableException(String message, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message, cause);
    }
}
