package com.example.netnovel_server.audio.exception;

import com.example.netnovel_server.exception.ApiException;
import org.springframework.http.HttpStatus;

public class AudioStorageException extends ApiException {

    public AudioStorageException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public AudioStorageException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}
