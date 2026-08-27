package com.jacm.aprobaciones.domain.ports.in;

import com.jacm.aprobaciones.domain.model.Aprobacion;

/**
 * Puerto de entrada: casos de uso relacionados con el rechazo de solicitudes.
 */
public interface RechazarSolicitudUseCase {

    /**
     * Rechaza una solicitud pendiente y publica el evento correspondiente.
     */
    Aprobacion rechazar(Long solicitudId, String aprobadorId, String motivo);
}
