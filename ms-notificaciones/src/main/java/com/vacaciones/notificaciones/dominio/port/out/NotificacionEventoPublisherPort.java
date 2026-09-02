package com.vacaciones.notificaciones.dominio.port.out;

import com.vacaciones.notificaciones.dominio.model.Notificacion;

public interface NotificacionEventoPublisherPort {

    void publicarResultado(Notificacion notificacion);
}
