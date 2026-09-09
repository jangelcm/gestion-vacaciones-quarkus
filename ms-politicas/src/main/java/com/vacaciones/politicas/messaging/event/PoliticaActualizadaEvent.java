package com.vacaciones.politicas.messaging.event;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PoliticaActualizadaEvent(
        Long politicaId,
        Long colaboradorId,
        LocalDate fechaInicioPolitica,
        String nombre,
        String tipoVacacion,
        Integer diasBaseAnio,
        Boolean activa,
        LocalDateTime fechaEvento) {
}
