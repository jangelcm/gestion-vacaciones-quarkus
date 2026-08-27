package com.vacaciones.politicas.exception;

import jakarta.ws.rs.core.Response;

public class BadRequestException extends RuntimeCustomException {

    public BadRequestException(String message) {
        super(message, Response.Status.BAD_REQUEST);
    }
}