package com.battledash.daemon.dynamic.errors;

public class BadRequestException extends HTTPException {
    {
        statusCode = 400;
        errorMessage = "Bad request.";
    }

    public BadRequestException() { }
    public BadRequestException(String reason) {
        this.errorMessage = reason;
    }
}
