package com.battledash.daemon.dynamic.errors;

public class NotFoundException extends HTTPException {
    {
        statusCode = 404;
        errorMessage = "Not found.";
    }

    public NotFoundException() { }
    public NotFoundException(String reason) {
        this.errorMessage = reason;
    }
}
