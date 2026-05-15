package com.gchoi.loyalty.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Maps application exceptions into consistent REST error responses.
 */
@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles duplicate purchase requests.
     */
    @ExceptionHandler(DuplicatePurchaseException.class)
    public ResponseEntity<ErrorDetails> handleDuplicatePurchase(
            DuplicatePurchaseException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request, exception);
    }

    /**
     * Handles requests for unknown customers.
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleCustomerNotFound(
            CustomerNotFoundException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, exception);
    }

    /**
     * Handles requests for unknown rewards.
     */
    @ExceptionHandler(RewardNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleRewardNotFound(
            RewardNotFoundException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, exception);
    }

    /**
     * Handles redemption requests that exceed available points.
     */
    @ExceptionHandler(InsufficientPointsException.class)
    public ResponseEntity<ErrorDetails> handleInsufficientPoints(
            InsufficientPointsException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request, exception);
    }

    /**
     * Handles requests for unknown purchases.
     */
    @ExceptionHandler(PurchaseNotFoundException.class)
    public ResponseEntity<ErrorDetails> handlePurchaseNotFound(
            PurchaseNotFoundException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, exception);
    }

    /**
     * Handles duplicate refund requests.
     */
    @ExceptionHandler(PurchaseAlreadyRefundedException.class)
    public ResponseEntity<ErrorDetails> handlePurchaseAlreadyRefunded(
            PurchaseAlreadyRefundedException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request, exception);
    }

    /**
     * Handles bean validation failures.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, detail, request, exception);
    }

    /**
     * Handles malformed business inputs.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request, exception);
    }

    /**
     * Handles requests for unknown routes or static resources.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDetails> handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, exception);
    }

    /**
     * Handles unexpected application failures.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleUnexpected(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, exception);
    }

    /**
     * Builds and logs a consistent API error response.
     */
    private ResponseEntity<ErrorDetails> error(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Exception exception
    ) {
        if (status.is5xxServerError()) {
            log.error("Exception handling request method={} path={} status={} message={}",
                    request.getMethod(), request.getRequestURI(), status.value(), message, exception);
        } else {
            log.warn("Exception handling request method={} path={} status={} message={} exception={}",
                    request.getMethod(), request.getRequestURI(), status.value(), message,
                    exception.getClass().getSimpleName());
        }
        ErrorDetails details = new ErrorDetails(
                new Date(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(details);
    }
}
