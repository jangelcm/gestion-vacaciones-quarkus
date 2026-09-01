package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.mapper;

import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.model.TipoNotificacion;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudAprobadaEvent;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudCreadaEvent;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudCanceladaEvent;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudRechazadaEvent;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventoNotificacionMapper {

    static final String ORIGEN_SOLICITUD_APROBADA = "solicitud.aprobada";
    static final String ORIGEN_SOLICITUD_CREADA = "solicitud.creada";
    static final String ORIGEN_SOLICITUD_RECHAZADA = "solicitud.rechazada";
    static final String ORIGEN_SOLICITUD_CANCELADA = "solicitud.cancelada";

    public Notificacion paraSolicitudAprobada(SolicitudAprobadaEvent evento) {
        Destinatario destinatario = new Destinatario(
                evento.colaboradorId(), evento.colaboradorEmail(), evento.colaboradorNombre());

        String asunto = "Tu solicitud de vacaciones fue aprobada";
        String cuerpo = "Hola " + evento.colaboradorNombre() + ", tu solicitud de vacaciones del "
                + evento.fechaInicio() + " al " + evento.fechaFin() + " fue aprobada.";

        return new Notificacion(
                evento.eventoId(),
                TipoNotificacion.EMAIL,
                destinatario,
                asunto,
                cuerpo,
                ORIGEN_SOLICITUD_APROBADA);
    }

    public Notificacion paraSolicitudCreada(SolicitudCreadaEvent evento) {
        Destinatario destinatario = new Destinatario(
                evento.colaboradorId(), evento.aprobadorEmail(), evento.aprobadorNombre());

        String asunto = "Nueva solicitud de vacaciones pendiente de aprobación";
        String cuerpo = "Hola " + evento.aprobadorNombre() + ", " + evento.colaboradorNombre()
                + " ha solicitado vacaciones del " + evento.fechaInicio() + " al " + evento.fechaFin()
                + ". Por favor revisa la solicitud.";

        return new Notificacion(
                evento.eventoId(),
                TipoNotificacion.EMAIL,
                destinatario,
                asunto,
                cuerpo,
                ORIGEN_SOLICITUD_CREADA);
    }

    public Notificacion paraSolicitudRechazada(SolicitudRechazadaEvent evento) {
        Destinatario destinatario = new Destinatario(
                evento.colaboradorId(), evento.colaboradorEmail(), evento.colaboradorNombre());

        String asunto = "Tu solicitud de vacaciones fue rechazada";
        String cuerpo = "Hola " + evento.colaboradorNombre() + ", tu solicitud de vacaciones del "
                + evento.fechaInicio() + " al " + evento.fechaFin() + " fue rechazada. Motivo: "
                + evento.motivoRechazo() + ".";

        return new Notificacion(
                evento.eventoId(),
                TipoNotificacion.EMAIL,
                destinatario,
                asunto,
                cuerpo,
                ORIGEN_SOLICITUD_RECHAZADA);
    }

    public Notificacion paraSolicitudCancelada(SolicitudCanceladaEvent evento) {
        Destinatario destinatario = new Destinatario(
                evento.colaboradorId(), evento.colaboradorEmail(), evento.colaboradorNombre());

        String asunto = "Tu solicitud de vacaciones fue cancelada";
        String cuerpo = "Hola " + evento.colaboradorNombre() + ", tu solicitud de vacaciones del "
                + evento.fechaInicio() + " al " + evento.fechaFin() + " fue cancelada.";

        return new Notificacion(
                evento.eventoId(),
                TipoNotificacion.EMAIL,
                destinatario,
                asunto,
                cuerpo,
                ORIGEN_SOLICITUD_CANCELADA);
    }
}
