package com.shopflow.producer.exception;

import com.shopflow.producer.config.CorrelationIdFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps business exceptions to RFC 7807 ProblemDetail responses.
 *
 * Every response carries the error code, the request's correlation id and
 * a timestamp, so a failure a caller reports can be found in the logs.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onValidationFailure(MethodArgumentNotValidException e) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST,
                "Validation failed", ErrorCode.VALIDATION_FAILED);
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail onMalformedBody(HttpMessageNotReadableException e) {
        return problem(HttpStatus.BAD_REQUEST,
                "Request body could not be parsed", ErrorCode.VALIDATION_FAILED);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail onOrderNotFound(OrderNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage(), ErrorCode.ORDER_NOT_FOUND);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail onProductNotFound(ProductNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage(), ErrorCode.PRODUCT_NOT_FOUND);
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ProblemDetail onInvalidState(InvalidOrderStateException e) {
        return problem(HttpStatus.CONFLICT, e.getMessage(), ErrorCode.INVALID_STATE_TRANSITION);
    }

    @ExceptionHandler(InvalidShippingDestinationException.class)
    public ProblemDetail onInvalidDestination(InvalidShippingDestinationException e) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(),
                ErrorCode.INVALID_SHIPPING_DESTINATION);
    }

    @ExceptionHandler(CatalogUnavailableException.class)
    public ProblemDetail onCatalogUnavailable(CatalogUnavailableException e) {
        log.warn("Catalogue unavailable: {}", e.getMessage());
        return problem(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(),
                ErrorCode.CATALOG_UNAVAILABLE);
    }

    /**
     * Last resort. The message is generic on purpose - internal detail
     * belongs in the logs, not in a client response.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail onUnexpected(Exception e) {
        log.error("Unhandled exception", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred", ErrorCode.INTERNAL_ERROR);
    }

    private ProblemDetail problem(HttpStatus status, String detail, ErrorCode code) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("code", code.name());
        problem.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }
}