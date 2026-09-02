package com.vacaciones.notificaciones.infraestructura.adaptadores.out.persistence;

import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import org.bson.types.ObjectId;

public class NotificacionPersistenceMapper {

    public NotificacionMongoEntity toEntity(Notificacion notificacion) {
        NotificacionMongoEntity entity = NotificacionMongoEntity.builder()
                .eventoId(notificacion.getEventoId())
                .colaboradorId(notificacion.getDestinatario().colaboradorId())
                .destinatarioEmail(notificacion.getDestinatario().email())
                .destinatarioNombre(notificacion.getDestinatario().nombre())
                .tipo(notificacion.getTipo())
                .asunto(notificacion.getAsunto())
                .cuerpo(notificacion.getCuerpo())
                .estado(notificacion.getEstado())
                .eventoOrigen(notificacion.getEventoOrigen())
                .fechaCreacion(notificacion.getFechaCreacion())
                .fechaEnvio(notificacion.getFechaEnvio())
                .build();

        if (notificacion.getId() != null) {
            entity.id = new ObjectId(notificacion.getId());
        }
        return entity;
    }

    public Notificacion toDominio(NotificacionMongoEntity entity) {
        Destinatario destinatario = new Destinatario(
                entity.getColaboradorId(), entity.getDestinatarioEmail(), entity.getDestinatarioNombre());

        return new Notificacion(
                entity.id != null ? entity.id.toHexString() : null,
                entity.getEventoId(),
                entity.getTipo(),
                destinatario,
                entity.getAsunto(),
                entity.getCuerpo(),
                entity.getEstado(),
                entity.getEventoOrigen(),
                entity.getFechaCreacion(),
                entity.getFechaEnvio());
    }
}
