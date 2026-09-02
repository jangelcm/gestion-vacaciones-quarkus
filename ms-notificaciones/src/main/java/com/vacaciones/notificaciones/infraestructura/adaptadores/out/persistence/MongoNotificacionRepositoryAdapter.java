package com.vacaciones.notificaciones.infraestructura.adaptadores.out.persistence;

import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.port.out.NotificacionRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class MongoNotificacionRepositoryAdapter implements NotificacionRepositoryPort {

    private final NotificacionPanacheRepository panacheRepository;
    private final NotificacionPersistenceMapper mapper;

    public MongoNotificacionRepositoryAdapter(NotificacionPanacheRepository panacheRepository) {
        this.panacheRepository = panacheRepository;
        this.mapper = new NotificacionPersistenceMapper();
    }

    @Override
    public Notificacion guardar(Notificacion notificacion) {
        NotificacionMongoEntity entity = mapper.toEntity(notificacion);
        panacheRepository.persist(entity);
        return mapper.toDominio(entity);
    }

    @Override
    public boolean existePorEventoId(String eventoId) {
        return panacheRepository.findByEventoId(eventoId).isPresent();
    }

    @Override
    public List<Notificacion> buscarPorColaborador(Long colaboradorId) {
        return panacheRepository.findByColaboradorId(colaboradorId).stream()
                .map(mapper::toDominio)
                .toList();
    }
}
