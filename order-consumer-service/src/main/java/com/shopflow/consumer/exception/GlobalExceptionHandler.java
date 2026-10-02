package com.shopflow.consumer.exception;

import com.shopflow.consumer.config.CorrelationIdFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * The consumer's HTTP surface is query-only, so this catalogue is small.
 * Message-processing failures are handled in the listeners, not here.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FulfilmentNotFoundException.class)
    public ProblemDetail onFulfilmentNotFound(FulfilmentNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage(), ErrorCode.FULFILMENT_NOT_FOUND);
    }

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