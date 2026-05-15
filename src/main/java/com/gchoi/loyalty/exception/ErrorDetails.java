package com.gchoi.loyalty.exception;

import lombok.Data;

import java.util.Date;

/**
 * Standard error response body returned by the API.
 */
@Data
public class ErrorDetails {
    private Date timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    /**
     * Creates an API error response.
     *
     * @param timestamp time the error response was created
     * @param status HTTP status code
     * @param error HTTP reason phrase
     * @param message human-readable error message
     * @param path request path that failed
     */
    public ErrorDetails(Date timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
