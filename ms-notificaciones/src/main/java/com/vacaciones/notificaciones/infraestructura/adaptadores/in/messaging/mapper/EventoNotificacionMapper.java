package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.mapper;

import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.model.TipoNotificacion;
import com.vacaciones.notificaciones.dominio.model.UsuarioInfo;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudAprobadaEvent;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudCreadaEvent;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudCanceladaEvent;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudRechazadaEvent;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class EventoNotificacionMapper {

    static final String ORIGEN_SOLICITUD_APROBADA = "solicitud.aprobada";
    static final String ORIGEN_SOLICITUD_CREADA = "solicitud.creada";
    static final String ORIGEN_SOLICITUD_RECHAZADA = "solicitud.rechazada";
    static final String ORIGEN_SOLICITUD_CANCELADA = "solicitud.cancelada";

    public Notificacion paraSolicitudAprobada(SolicitudAprobadaEvent evento, UsuarioInfo usuario) {
        Destinatario destinatario = new Destinatario(
                evento.colaboradorId(), usuario.email(), usuario.nombre());

        String asunto = "Tu solicitud de vacaciones fue aprobada";
        String cuerpo = "Hola " + usuario.nombre() + ", tu solicitud de vacaciones del "
                + evento.fechaInicio() + " al " + evento.fechaFin() + " fue aprobada.";

        return new Notificacion(
                evento.eventoId(),
                TipoNotificacion.RECORDATORIO,
                destinatario,
                asunto,
                cuerpo,
                ORIGEN_SOLICITUD_APROBADA);
    }

    /**
     * A diferencia de aprobada/rechazada/cancelada (un solo destinatario, el colaborador), aqui
     * el destinatario es "todo aprobador" (rol Administrador): se genera una Notificacion por
     * cada uno. El eventoId se compone con el id del aprobador para que la idempotencia en
     * EnviarNotificacionService no descarte las notificaciones de todos menos el primero.
     */
    public List<Notificacion> paraSolicitudCreada(SolicitudCreadaEvent evento, List<UsuarioInfo> aprobadores) {
        String asunto = "Nueva solicitud de vacaciones pendiente de aprobación";
        String cuerpo = "El colaborador con ID " + evento.colaboradorId() + " ha solicitado vacaciones del "
                + evento.fechaInicio() + " al " + evento.fechaFin() + ". Por favor revisa la solicitud.";

        return aprobadores.stream()
                .map(aprobador -> new Notificacion(
                        evento.id() != null ? evento.id() + ":" + aprobador.id() : null,
                        TipoNotificacion.RECORDATORIO,
                        new Destinatario(aprobador.id(), aprobador.email(), aprobador.nombre()),
                        asunto,
                        cuerpo,
                        ORIGEN_SOLICITUD_CREADA))
                .toList();
    }

    public Notificacion paraSolicitudRechazada(SolicitudRechazadaEvent evento, UsuarioInfo usuario) {
        Destinatario destinatario = new Destinatario(
                evento.colaboradorId(), usuario.email(), usuario.nombre());

        String asunto = "Tu solicitud de vacaciones fue rechazada";
        String cuerpo = "Hola " + usuario.nombre() + ", tu solicitud de vacaciones del "
                + evento.fechaInicio() + " al " + evento.fechaFin() + " fue rechazada. Motivo: "
                + evento.comentario() + ".";

        return new Notificacion(
                evento.solicitudId() != null ? "rechazada:" + evento.solicitudId() : null,
                TipoNotificacion.RECORDATORIO,
                destinatario,
                asunto,
                cuerpo,
                ORIGEN_SOLICITUD_RECHAZADA);
    }

    public Notificacion paraSolicitudCancelada(SolicitudCanceladaEvent evento, UsuarioInfo usuario) {
        Destinatario destinatario = new Destinatario(
                evento.colaboradorId(), usuario.email(), usuario.nombre());

        String asunto = "Tu solicitud de vacaciones fue cancelada";
        String cuerpo = "Hola " + usuario.nombre() + ", tu solicitud de vacaciones del "
                + evento.fechaInicio() + " al " + evento.fechaFin() + " fue cancelada.";

        return new Notificacion(
                evento.eventoId(),
                TipoNotificacion.RECORDATORIO,
                destinatario,
                asunto,
                cuerpo,
                ORIGEN_SOLICITUD_CANCELADA);
    }
}
