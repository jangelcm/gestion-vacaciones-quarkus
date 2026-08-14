package com.jacm.solicitudes.domain.service;


import com.jacm.solicitudes.api.dto.Solicitud;
import com.jacm.solicitudes.domain.model.EstadoSolicitud;
import com.jacm.solicitudes.domain.model.SolicitudEntity;
import com.jacm.solicitudes.infrastructure.messaging.producer.SolicitudEventProducer;
import com.jacm.solicitudes.infrastructure.repository.SolicitudRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class SolicitudServiceImpl implements SolicitudService{
    @Inject
    SolicitudRepository repository;

    @Inject
    SolicitudEventProducer solicitudEventProducer;

    @Override
    public Solicitud crearSolicitud(Solicitud solicitud) {
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        var entity = SolicitudEntity.fromDomain(solicitud);
        repository.persist(entity);
        solicitud.setId(entity.id);
        solicitudEventProducer.enviarSolicitudCreada(solicitud);

        return solicitud;
    }

    @Override
    public Solicitud obtenerPorId(Long id){
        var entity = repository.findById(id);
        if(entity == null){
            throw new NoSuchElementException("Solicitud no encontrada con id: " + id);
        }
        return entity.toDomain();
    }

    @Override
    public List<Solicitud> listarPorColaborador(Long colaboradorId){
        return this.repository.list("colaboradorId",colaboradorId).stream().map(SolicitudEntity::toDomain).collect(Collectors.toList());
    }

    public void actualizarEstado(Long id, EstadoSolicitud nuevoEstadoo){
        this.repository.findByIdOptional(id).ifPresent(solicitud ->
                solicitud.setEstado(nuevoEstadoo));
    }


}
