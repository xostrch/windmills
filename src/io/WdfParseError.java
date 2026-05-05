package io;

public class WdfParseError extends RuntimeException {
    public WdfParseError(String message) {
        super(message);
    }

    public WdfParseError(String message, Throwable cause) {
        super(message, cause);
    }
}