package com.jacm.aprobaciones.infrastructure.adapters.out.kafka;

import com.jacm.aprobaciones.domain.ports.out.AprobacionEventPublisherPort;
import com.jacm.aprobaciones.infrastructure.adapters.out.kafka.dto.AprobacionEventoDTO;
import com.jacm.aprobaciones.infrastructure.adapters.out.kafka.dto.SolicitudAprobadaEventoDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

/**
 * Adaptador Kafka: implementa el puerto de salida {@link AprobacionEventPublisherPort}.
 * Publica eventos hacia los topics 'solicitud.aprobada' y 'solicitud.rechazada'.
 */
@ApplicationScoped
public class AprobacionEventPublisherAdapter implements AprobacionEventPublisherPort {

    private static final Logger LOG = Logger.getLogger(AprobacionEventPublisherAdapter.class);

    @Inject
    @Channel("solicitud-aprobada")
    Emitter<SolicitudAprobadaEventoDTO> aprobadaEmitter;

    @Inject
    @Channel("solicitud-rechazada")
    Emitter<AprobacionEventoDTO> rechazadaEmitter;

    @Override
    public void publicarSolicitudAprobada(
            Long solicitudId, String colaboradorId, BigDecimal diasAprobados, String aprobadorId, String comentario) {
        var evento = new SolicitudAprobadaEventoDTO(
                UUID.randomUUID().toString(),
                solicitudId,
                colaboradorId != null ? Long.valueOf(colaboradorId) : null,
                diasAprobados,
                LocalDateTime.now());
        LOG.infof("Publicando evento 'solicitud.aprobada' para solicitud ID: %d, colaborador: %s, dias: %s",
                solicitudId, colaboradorId, diasAprobados);
        aprobadaEmitter.send(evento);
    }

    @Override
    public void publicarSolicitudRechazada(Long solicitudId, String aprobadorId, String motivo) {
        var evento = new AprobacionEventoDTO(solicitudId, aprobadorId, "RECHAZADO", motivo);
        LOG.infof("Publicando evento 'solicitud.rechazada' para solicitud ID: %d", solicitudId);
        rechazadaEmitter.send(evento);
    }
}
