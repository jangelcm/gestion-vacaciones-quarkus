package org.acme.messaging.event;

import java.time.LocalDate;

public record EmpleadoCreadoEvent(
        Long trabajadorId,
        LocalDate fechaIngreso,
        Boolean esDefault) {
}
