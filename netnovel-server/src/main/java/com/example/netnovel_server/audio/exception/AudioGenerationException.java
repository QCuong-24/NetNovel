package com.example.netnovel_server.audio.exception;

import com.example.netnovel_server.exception.ApiException;
import org.springframework.http.HttpStatus;

public class AudioGenerationException extends ApiException {

    public AudioGenerationException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }

    public AudioGenerationException(String message, Throwable cause) {
        super(HttpStatus.BAD_GATEWAY, message, cause);
    }
}
