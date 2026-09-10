package com.vacaciones.politicas.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacaciones.politicas.messaging.event.SolicitudCreadaEvent;
import com.vacaciones.politicas.messaging.event.SolicitudAprobadaEvent;
import com.vacaciones.politicas.service.SaldoDiasService;
import com.vacaciones.politicas.service.ValidacionService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SolicitudAprobadaConsumer {

    private static final Logger LOG = Logger.getLogger(SolicitudAprobadaConsumer.class);

    private final ObjectMapper objectMapper;
    private final SaldoDiasService saldoDiasService;
    private final ValidacionService validacionService;

    public SolicitudAprobadaConsumer(ObjectMapper objectMapper, SaldoDiasService saldoDiasService, ValidacionService validacionService) {
        this.objectMapper = objectMapper;
        this.saldoDiasService = saldoDiasService;
        this.validacionService = validacionService;
    }

    @Incoming("solicitud-creada-in")
    public void onSolicitudCreada(String mensaje) throws JsonProcessingException {
        LOG.infof("Evento 'solicitud.creada' recibido: %s", mensaje);
        SolicitudCreadaEvent evento = objectMapper.readValue(mensaje, SolicitudCreadaEvent.class);
        long diasSolicitados = validacionService.calcularDiasHabiles(evento.fechaInicio(), evento.fechaFin());
        saldoDiasService.reservarDiasPorSolicitudCreada(
                Long.valueOf(evento.colaboradorId()),
                evento.id(),
                java.math.BigDecimal.valueOf(diasSolicitados),
                "solicitud.creada:" + evento.id());
    }

    @Incoming("solicitud-aprobada-in")
    public void onSolicitudAprobada(String mensaje) throws JsonProcessingException {
        LOG.infof("Evento 'solicitud.aprobada' recibido: %s", mensaje);
        SolicitudAprobadaEvent evento = objectMapper.readValue(mensaje, SolicitudAprobadaEvent.class);
        saldoDiasService.procesarSolicitudAprobada(evento);
    }
}
