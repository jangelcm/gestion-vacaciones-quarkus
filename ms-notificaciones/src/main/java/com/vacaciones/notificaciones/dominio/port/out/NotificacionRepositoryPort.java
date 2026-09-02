package com.vacaciones.notificaciones.dominio.port.out;

import com.vacaciones.notificaciones.dominio.model.Notificacion;
import java.util.List;

public interface NotificacionRepositoryPort {

    Notificacion guardar(Notificacion notificacion);

    boolean existePorEventoId(String eventoId);

    List<Notificacion> buscarPorColaborador(Long colaboradorId);
}
