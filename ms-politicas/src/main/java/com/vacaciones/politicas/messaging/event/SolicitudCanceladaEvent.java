package com.vacaciones.politicas.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SolicitudCanceladaEvent(
        String eventoId,
        Long solicitudId,
        Long colaboradorId,
        BigDecimal diasADevolver,
        LocalDateTime fechaCancelacion) {
}
