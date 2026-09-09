package com.jacm.consultas.service;

import com.jacm.consultas.messaging.dto.SolicitudCreadaEvent;
import com.jacm.consultas.messaging.dto.DiasDisponiblesActualizadosEvent;
import com.jacm.consultas.messaging.dto.PoliticaActualizadaEvent;
import com.jacm.consultas.model.PoliticaReadDocument;
import com.jacm.consultas.model.SaldoVacacionalReadDocument;
import com.jacm.consultas.model.SolicitudHistorialDocument;
import com.jacm.consultas.model.SolicitudReadDocument;
import com.jacm.consultas.repository.PoliticaReadRepository;
import com.jacm.consultas.repository.SaldoVacacionalReadRepository;
import com.jacm.consultas.repository.SolicitudHistorialRepository;
import com.jacm.consultas.repository.SolicitudReadRepository;
import com.jacm.consultas.websocket.ConsultaUpdatesNotifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@ApplicationScoped
public class ConsultasProjectionService {

    @Inject
    SolicitudReadRepository solicitudReadRepository;

    @Inject
    SolicitudHistorialRepository solicitudHistorialRepository;

    @Inject
    SaldoVacacionalReadRepository saldoVacacionalReadRepository;

    @Inject
    PoliticaReadRepository politicaReadRepository;

    @Inject
    ConsultaUpdatesNotifier consultaUpdatesNotifier;

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
        consultaUpdatesNotifier.notificar(parseColaboradorId(doc.colaboradorId), "solicitud.creada", doc);
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
        consultaUpdatesNotifier.notificar(parseColaboradorId(doc.colaboradorId), "solicitud.estado.actualizado", doc);
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

    public void proyectarSaldoActualizado(DiasDisponiblesActualizadosEvent event) {
        SaldoVacacionalReadDocument doc = saldoVacacionalReadRepository.findById(event.colaboradorId());
        if (doc == null) {
            doc = new SaldoVacacionalReadDocument();
            doc.colaboradorId = event.colaboradorId();
        }

        doc.politicaId = event.politicaId();
        doc.fechaInicioPolitica = event.fechaInicioPolitica();
        doc.diasGozados = toScale(event.diasUsados());
        doc.diasHabilitados = toScale(event.diasHabilitados());
        doc.saldoActual = toScale(event.saldoActual());
        doc.diasTruncos = toScale(event.diasTruncos());
        doc.diasTrabajados = event.diasTrabajados() == null ? 0 : event.diasTrabajados();
        doc.diasPendientes = toScale(event.diasPendientes());
        doc.motivoActualizacion = event.motivoActualizacion();
        doc.ultimaActualizacion = event.fechaEvento() != null ? event.fechaEvento() : LocalDateTime.now();

        saldoVacacionalReadRepository.persistOrUpdate(doc);
        consultaUpdatesNotifier.notificar(event.colaboradorId(), "dias.disponibles.actualizados", doc);
    }

    public void proyectarPoliticaActualizada(PoliticaActualizadaEvent event) {
        PoliticaReadDocument doc = politicaReadRepository.findById(event.politicaId());
        if (doc == null) {
            doc = new PoliticaReadDocument();
            doc.politicaId = event.politicaId();
        }
        doc.nombre = event.nombre();
        doc.tipoVacacion = event.tipoVacacion();
        doc.diasBaseAnio = event.diasBaseAnio();
        doc.activa = event.activa();
        doc.ultimaActualizacion = event.fechaEvento() != null ? event.fechaEvento() : LocalDateTime.now();
        politicaReadRepository.persistOrUpdate(doc);
    }

    public SaldoVacacionalReadDocument obtenerSaldoVacacional(Long colaboradorId) {
        SaldoVacacionalReadDocument doc = saldoVacacionalReadRepository.findById(colaboradorId);
        if (doc == null) {
            throw new NoSuchElementException("Saldo vacacional no encontrado para colaboradorId: " + colaboradorId);
        }
        return doc;
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

    private Long parseColaboradorId(String colaboradorId) {
        if (colaboradorId == null || colaboradorId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(colaboradorId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal toScale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(1) : value.setScale(1);
    }
}
