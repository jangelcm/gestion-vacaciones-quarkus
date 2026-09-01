package com.vacaciones.notificaciones.infraestructura.adaptadores.out.persistence;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class NotificacionPanacheRepository implements PanacheMongoRepository<NotificacionMongoEntity> {

    public Optional<NotificacionMongoEntity> findByEventoId(String eventoId) {
        return find("eventoId", eventoId).firstResultOptional();
    }

    public List<NotificacionMongoEntity> findByColaboradorId(Long colaboradorId) {
        return find("colaboradorId", colaboradorId).list();
    }
}
