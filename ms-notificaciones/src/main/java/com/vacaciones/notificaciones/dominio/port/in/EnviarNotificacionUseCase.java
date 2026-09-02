package com.vacaciones.notificaciones.dominio.port.in;

import com.vacaciones.notificaciones.dominio.model.Notificacion;

public interface EnviarNotificacionUseCase {

    void enviar(Notificacion notificacion);
}
