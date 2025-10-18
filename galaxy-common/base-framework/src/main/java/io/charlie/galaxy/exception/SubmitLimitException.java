package io.charlie.galaxy.exception;

public class SubmitLimitException extends RuntimeException {
    public SubmitLimitException(String message) {
        super(message);
    }
}