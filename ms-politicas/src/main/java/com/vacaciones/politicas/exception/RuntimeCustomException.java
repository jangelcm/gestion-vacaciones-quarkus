package com.vacaciones.politicas.exception;

import jakarta.ws.rs.core.Response;

public class RuntimeCustomException extends RuntimeException {

    private final Response.Status status;

    public RuntimeCustomException(String message) {
        this(message, Response.Status.INTERNAL_SERVER_ERROR);
    }

    public RuntimeCustomException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }
}