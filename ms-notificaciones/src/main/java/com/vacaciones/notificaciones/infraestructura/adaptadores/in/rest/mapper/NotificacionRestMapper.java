package com.vacaciones.notificaciones.infraestructura.adaptadores.in.rest.mapper;

import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.rest.dto.request.NotificacionTestRequestDto;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.rest.dto.response.NotificacionResponseDto;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class NotificacionRestMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public Notificacion toDominio(NotificacionTestRequestDto dto) {
        Destinatario destinatario = new Destinatario(dto.colaboradorId(), dto.email(), dto.nombre());
        return new Notificacion(null, dto.tipo(), destinatario, dto.asunto(), dto.cuerpo(), null);
    }

    public NotificacionResponseDto toResponseDto(Notificacion notificacion) {
        return new NotificacionResponseDto(
                notificacion.getId(),
                notificacion.getEventoId(),
                notificacion.getTipo(),
                notificacion.getDestinatario(),
                notificacion.getAsunto(),
                notificacion.getCuerpo(),
                notificacion.getEstado(),
                notificacion.getEventoOrigen(),
                notificacion.getFechaCreacion() != null ? notificacion.getFechaCreacion().format(FORMATTER) : null,
                notificacion.getFechaEnvio() != null ? notificacion.getFechaEnvio().format(FORMATTER) : null);
    }
}
