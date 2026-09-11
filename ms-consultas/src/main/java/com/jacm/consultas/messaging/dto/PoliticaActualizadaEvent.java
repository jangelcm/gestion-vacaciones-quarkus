package com.jacm.consultas.messaging.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PoliticaActualizadaEvent(
        Long politicaId,
        Long colaboradorId,
        LocalDate fechaInicioPolitica,
        String nombre,
        String tipoVacacion,
        Integer diasBaseAnio,
        Integer antiguedadMinimaMeses,
        Boolean acumulable,
        Integer maxDiasAcumulables,
        Boolean activa,
        LocalDateTime fechaEvento) {
}
