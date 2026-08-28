package com.vacaciones.politicas.messaging.event;

import java.time.LocalDateTime;

public record PoliticaActualizadaEvent(
        Long politicaId,
        String nombre,
        String tipoVacacion,
        Integer diasBaseAnio,
        Boolean activa,
        LocalDateTime fechaEvento) {
}
