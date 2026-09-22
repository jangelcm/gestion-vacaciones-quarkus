package com.vacaciones.politicas.messaging.event;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDate;

@RegisterForReflection
public record EmpleadoCreadoEvent(
        Long trabajadorId,
        LocalDate fechaIngreso,
        Boolean esDefault) {
}
