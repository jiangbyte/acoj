package io.charlie.cores.exception;

public class SubmitLimitException extends RuntimeException {
    public SubmitLimitException(String message) {
        super(message);
    }
}