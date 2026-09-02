package com.vacaciones.notificaciones.aplicacion.casouso;

import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.port.in.ConsultarHistorialUseCase;
import com.vacaciones.notificaciones.dominio.port.out.NotificacionRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ConsultarHistorialService implements ConsultarHistorialUseCase {

    private final NotificacionRepositoryPort repository;

    public ConsultarHistorialService(NotificacionRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<Notificacion> consultarPorColaborador(Long colaboradorId) {
        return repository.buscarPorColaborador(colaboradorId);
    }
}
