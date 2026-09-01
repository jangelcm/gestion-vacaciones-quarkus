package com.vacaciones.notificaciones.dominio.port.out;

public interface NotificadorTiempoRealPort {

    void notificar(Long colaboradorId, String tipoEvento, Object payload);
}
