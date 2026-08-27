package com.jacm.aprobaciones.domain.ports.in;

import com.jacm.aprobaciones.domain.model.Aprobacion;

/**
 * Puerto de entrada: casos de uso relacionados con la aprobación de solicitudes.
 */
public interface AprobarSolicitudUseCase {

    /**
     * Registra una nueva aprobación en estado PENDIENTE al recibir el evento
     * de solicitud creada desde Kafka.
     */
    Aprobacion registrarParaAprobacion(Long solicitudId);

    /**
     * Aprueba una solicitud pendiente y publica el evento correspondiente.
     */
    Aprobacion aprobar(Long solicitudId, String aprobadorId, String comentario);
}
