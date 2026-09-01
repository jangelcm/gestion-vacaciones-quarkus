package com.vacaciones.notificaciones.dominio.port.in;

import com.vacaciones.notificaciones.dominio.model.Notificacion;
import java.util.List;

public interface ConsultarHistorialUseCase {

    List<Notificacion> consultarPorColaborador(Long colaboradorId);
}
