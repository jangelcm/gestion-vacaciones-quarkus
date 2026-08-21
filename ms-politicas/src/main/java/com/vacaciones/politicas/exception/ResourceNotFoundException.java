package com.vacaciones.politicas.exception;

import jakarta.ws.rs.core.Response;

public class ResourceNotFoundException extends RuntimeCustomException {

    public ResourceNotFoundException(String message) {
        super(message, Response.Status.NOT_FOUND);
    }
}