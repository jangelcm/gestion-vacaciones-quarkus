package com.vacaciones.politicas.exception;

import jakarta.ws.rs.core.Response;

public class RequestValidationException extends RuntimeCustomException {

    public RequestValidationException(String message) {
        super(message, Response.Status.BAD_REQUEST);
    }
}