package com.vacaciones.politicas.exception;

public class SaldoInsuficienteException extends BadRequestException {

    public SaldoInsuficienteException(String message) {
        super(message);
    }
}