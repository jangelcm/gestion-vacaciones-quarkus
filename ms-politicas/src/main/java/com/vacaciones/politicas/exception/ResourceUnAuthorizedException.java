package com.vacaciones.politicas.exception;

import jakarta.ws.rs.core.Response;

public class ResourceUnAuthorizedException extends RuntimeCustomException {

    public ResourceUnAuthorizedException(String message) {
        super(message, Response.Status.UNAUTHORIZED);
    }
}