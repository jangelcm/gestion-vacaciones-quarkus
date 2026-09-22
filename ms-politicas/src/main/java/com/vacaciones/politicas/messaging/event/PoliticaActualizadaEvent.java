package com.vacaciones.politicas.messaging.event;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RegisterForReflection
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
