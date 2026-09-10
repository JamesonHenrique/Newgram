package com.jhcs.newgram.infrastructure.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/** Envelope padrao de erro da API (inclui traceId para correlacao em log). */
public class ApiError {
    private final int status;
    private final String message;
    private final String path;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;
    private final String traceId;

    public ApiError(int status, String message, String path, LocalDateTime timestamp) {
        this(status, message, path, timestamp, null);
    }

    public ApiError(int status, String message, String path, LocalDateTime timestamp, String traceId) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
        this.traceId = traceId;
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

    public String getTraceId() {
        return traceId;
    }
}
