package com.jacm.aprobaciones.domain.ports.out;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Puerto de salida: publicación de eventos de aprobación hacia Kafka.
 * Implementado en infrastructure/adapters/out/kafka/.
 */
public interface AprobacionEventPublisherPort {

    void publicarSolicitudAprobada(Long solicitudId, String colaboradorId, BigDecimal diasAprobados,
            LocalDate fechaInicio, LocalDate fechaFin, String aprobadorId, String comentario);

    void publicarSolicitudRechazada(Long solicitudId, String colaboradorId, LocalDate fechaInicio,
            LocalDate fechaFin, String aprobadorId, String motivo);
}
