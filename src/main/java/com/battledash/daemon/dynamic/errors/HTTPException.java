package com.battledash.daemon.dynamic.errors;

/**
 * An HTTP Exception to be thrown in Jersey REST handlers.
 */
public abstract class HTTPException extends RuntimeException {
    public int statusCode = 500;
    public String errorMessage = "An unknown error occurred.";

    public HTTPException() { }
    public HTTPException(String reason) {
        this.errorMessage = reason;
    }

    @Override
    public String toString() {
        return errorMessage;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
