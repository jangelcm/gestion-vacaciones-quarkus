package com.vacaciones.notificaciones.dominio.port.in;

import com.vacaciones.notificaciones.dominio.model.Adjunto;
import com.vacaciones.notificaciones.dominio.model.Notificacion;

public interface EnviarNotificacionUseCase {

    void enviar(Notificacion notificacion);

    /** Igual que {@link #enviar}, pero adjuntando un archivo al email (ej. comprobante en PDF). */
    void enviarConAdjunto(Notificacion notificacion, Adjunto adjunto);
}
