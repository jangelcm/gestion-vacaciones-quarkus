package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacaciones.notificaciones.aplicacion.casouso.ComprobanteVacacionesPdfService;
import com.vacaciones.notificaciones.dominio.model.Adjunto;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.model.UsuarioInfo;
import com.vacaciones.notificaciones.dominio.port.in.EnviarNotificacionUseCase;
import com.vacaciones.notificaciones.dominio.port.out.ResolverUsuarioPort;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudAprobadaEvent;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.mapper.EventoNotificacionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SolicitudAprobadaConsumer {

    private static final Logger LOG = Logger.getLogger(SolicitudAprobadaConsumer.class);

    private final ObjectMapper objectMapper;
    private final EventoNotificacionMapper eventoNotificacionMapper;
    private final EnviarNotificacionUseCase enviarNotificacionUseCase;
    private final ResolverUsuarioPort resolverUsuarioPort;
    private final ComprobanteVacacionesPdfService comprobanteVacacionesPdfService;

    public SolicitudAprobadaConsumer(
            ObjectMapper objectMapper,
            EventoNotificacionMapper eventoNotificacionMapper,
            EnviarNotificacionUseCase enviarNotificacionUseCase,
            ResolverUsuarioPort resolverUsuarioPort,
            ComprobanteVacacionesPdfService comprobanteVacacionesPdfService) {
        this.objectMapper = objectMapper;
        this.eventoNotificacionMapper = eventoNotificacionMapper;
        this.enviarNotificacionUseCase = enviarNotificacionUseCase;
        this.resolverUsuarioPort = resolverUsuarioPort;
        this.comprobanteVacacionesPdfService = comprobanteVacacionesPdfService;
    }

    @Incoming("solicitud-aprobada-in")
    public void onSolicitudAprobada(String mensaje) throws JsonProcessingException {
        LOG.infof("Evento 'solicitud.aprobada' recibido: %s", mensaje);
        SolicitudAprobadaEvent evento = objectMapper.readValue(mensaje, SolicitudAprobadaEvent.class);
        UsuarioInfo usuario = resolverUsuarioPort.resolverPorColaboradorId(evento.colaboradorId())
                .orElseGet(() -> new UsuarioInfo(evento.colaboradorId(), null, "Colaborador " + evento.colaboradorId()));
        Notificacion notificacion = eventoNotificacionMapper.paraSolicitudAprobada(evento, usuario);

        Adjunto comprobante = generarComprobante(evento, usuario);
        if (comprobante != null) {
            enviarNotificacionUseCase.enviarConAdjunto(notificacion, comprobante);
        } else {
            enviarNotificacionUseCase.enviar(notificacion);
        }
    }

    /** Best-effort: si no se puede generar el comprobante, el email de aprobacion igual se manda sin adjunto. */
    private Adjunto generarComprobante(SolicitudAprobadaEvent evento, UsuarioInfo colaborador) {
        try {
            String nombreAprobador = resolverNombreAprobador(evento.aprobadorId());
            // Dias CALENDARIO del rango mostrado en el propio comprobante (fecha inicio -> fecha
            // fin), no evento.diasAprobados(): ese campo son dias HABILES (lun-vie, para
            // descontar del saldo) y puede no coincidir con el rango de fechas impreso al lado,
            // lo cual es justamente la inconsistencia que un comprobante no puede mostrar.
            BigDecimal dias = BigDecimal.valueOf(ChronoUnit.DAYS.between(evento.fechaInicio(), evento.fechaFin()) + 1);

            byte[] pdf = comprobanteVacacionesPdfService.generar(
                    colaborador.nombre(), evento.fechaInicio(), evento.fechaFin(), dias, nombreAprobador);
            return new Adjunto("comprobante-vacaciones.pdf", pdf, "application/pdf");
        } catch (RuntimeException e) {
            LOG.warnf(e, "No se pudo generar el comprobante de vacaciones para la solicitud %d, se envia el email sin adjunto",
                    evento.solicitudId());
            return null;
        }
    }

    private String resolverNombreAprobador(String aprobadorId) {
        if (aprobadorId == null || aprobadorId.isBlank()) {
            return null;
        }
        try {
            return resolverUsuarioPort.resolverPorColaboradorId(Long.valueOf(aprobadorId))
                    .map(UsuarioInfo::nombre)
                    .orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
