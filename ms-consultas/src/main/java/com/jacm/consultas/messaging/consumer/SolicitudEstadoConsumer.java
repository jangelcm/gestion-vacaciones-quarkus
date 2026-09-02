package com.jacm.consultas.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacm.consultas.messaging.dto.AprobacionEvento;
import com.jacm.consultas.service.ConsultasProjectionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SolicitudEstadoConsumer {

    private static final Logger LOG = Logger.getLogger(SolicitudEstadoConsumer.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ConsultasProjectionService consultasProjectionService;

    @Incoming("solicitud-aprobada-in")
    public void onSolicitudAprobada(String mensaje) {
        procesarAprobacion(mensaje, "APROBADA", "Solicitud aprobada");
    }

    @Incoming("solicitud-rechazada-in")
    public void onSolicitudRechazada(String mensaje) {
        procesarAprobacion(mensaje, "RECHAZADA", "Solicitud rechazada");
    }

    @Incoming("solicitud-cancelada-in")
    public void onSolicitudCancelada(String mensaje) {
        try {
            JsonNode json = objectMapper.readTree(mensaje);
            Long solicitudId = extraerSolicitudId(json);
            String detalle = json.hasNonNull("comentario") ? json.get("comentario").asText() : "Solicitud cancelada";
            consultasProjectionService.proyectarCambioEstado(solicitudId, "CANCELADA", detalle);
            LOG.infof("Read model actualizado para solicitud.cancelada id=%d", solicitudId);
        } catch (Exception e) {
            LOG.errorf("Error procesando solicitud.cancelada: %s", e.getMessage());
        }
    }

    private void procesarAprobacion(String mensaje, String estadoDefault, String detalleDefault) {
        try {
            AprobacionEvento event = objectMapper.readValue(mensaje, AprobacionEvento.class);
            String detalle = (event.comentario() == null || event.comentario().isBlank())
                    ? detalleDefault
                    : event.comentario();
            String estado = (event.estado() == null || event.estado().isBlank())
                    ? estadoDefault
                    : event.estado();

            consultasProjectionService.proyectarCambioEstado(event.solicitudId(), estado, detalle);
            LOG.infof("Read model actualizado para %s id=%d", estado, event.solicitudId());
        } catch (JsonProcessingException e) {
            LOG.errorf("Error deserializando evento de estado: %s", e.getMessage());
        } catch (Exception e) {
            LOG.errorf("Error actualizando estado de solicitud: %s", e.getMessage());
        }
    }

    private Long extraerSolicitudId(JsonNode json) {
        if (json.hasNonNull("solicitudId")) {
            return json.get("solicitudId").asLong();
        }
        if (json.hasNonNull("id")) {
            return json.get("id").asLong();
        }
        throw new IllegalArgumentException("Evento sin campo solicitudId o id");
    }
}
