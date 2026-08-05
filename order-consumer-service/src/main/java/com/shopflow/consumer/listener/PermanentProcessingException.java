package com.shopflow.consumer.listener;

/**
 * A failure that retrying cannot fix - malformed payload, embargoed
 * destination. Goes straight to the DLQ.
 */
public class PermanentProcessingException extends RuntimeException {

    public PermanentProcessingException(String message) {
        super(message);
    }
}