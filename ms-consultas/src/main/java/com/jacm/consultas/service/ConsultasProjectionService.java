package com.jacm.consultas.service;

import com.jacm.consultas.messaging.dto.SolicitudCreadaEvent;
import com.jacm.consultas.model.SolicitudHistorialDocument;
import com.jacm.consultas.model.SolicitudReadDocument;
import com.jacm.consultas.repository.SolicitudHistorialRepository;
import com.jacm.consultas.repository.SolicitudReadRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@ApplicationScoped
public class ConsultasProjectionService {

    @Inject
    SolicitudReadRepository solicitudReadRepository;

    @Inject
    SolicitudHistorialRepository solicitudHistorialRepository;

    public void proyectarSolicitudCreada(SolicitudCreadaEvent event) {
        SolicitudReadDocument doc = solicitudReadRepository.findById(event.id());
        if (doc == null) {
            doc = new SolicitudReadDocument();
            doc.solicitudId = event.id();
        }

        doc.colaboradorId = event.colaboradorId();
        doc.fechaInicio = event.fechaInicio();
        doc.fechaFin = event.fechaFin();
        doc.fechaSolicitud = event.fechaSolicitud();
        doc.estado = normalizarEstado(event.estado());
        doc.ultimaActualizacion = LocalDateTime.now();

        solicitudReadRepository.persistOrUpdate(doc);
        registrarHistorial(event.id(), doc.estado, "Solicitud creada");
    }

    public void proyectarCambioEstado(Long solicitudId, String estado, String detalle) {
        SolicitudReadDocument doc = solicitudReadRepository.findById(solicitudId);
        if (doc == null) {
            throw new NoSuchElementException("No existe read model para solicitudId: " + solicitudId);
        }

        doc.estado = normalizarEstado(estado);
        doc.ultimaActualizacion = LocalDateTime.now();
        solicitudReadRepository.persistOrUpdate(doc);

        registrarHistorial(solicitudId, doc.estado, detalle);
    }

    public SolicitudReadDocument obtenerSolicitud(Long solicitudId) {
        SolicitudReadDocument doc = solicitudReadRepository.findById(solicitudId);
        if (doc == null) {
            throw new NoSuchElementException("Solicitud no encontrada con id: " + solicitudId);
        }
        return doc;
    }

    public List<SolicitudReadDocument> listarSolicitudesPorColaborador(String colaboradorId) {
        return solicitudReadRepository.listarPorColaboradorId(colaboradorId);
    }

    public List<SolicitudHistorialDocument> listarHistorial(Long solicitudId) {
        return solicitudHistorialRepository.listarPorSolicitudId(solicitudId);
    }

    private void registrarHistorial(Long solicitudId, String estado, String detalle) {
        SolicitudHistorialDocument historial = new SolicitudHistorialDocument();
        historial.solicitudId = solicitudId;
        historial.estado = estado;
        historial.detalle = detalle;
        historial.fechaEvento = LocalDateTime.now();
        solicitudHistorialRepository.persist(historial);
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return "PENDIENTE";
        }

        String normalizado = estado.toUpperCase();
        if ("APROBADO".equals(normalizado)) {
            return "APROBADA";
        }
        if ("RECHAZADO".equals(normalizado)) {
            return "RECHAZADA";
        }
        return normalizado;
    }
}
