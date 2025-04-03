package com.jhcs.newgram.infrastructure.exception;

import java.time.LocalDateTime;

public  class ApiError {
    private final int status;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;

    public ApiError(int status, String message, String path, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}