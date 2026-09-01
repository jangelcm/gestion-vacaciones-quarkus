package com.vacaciones.notificaciones.infraestructura.adaptadores.out.persistence;

import com.vacaciones.notificaciones.dominio.model.EstadoNotificacion;
import com.vacaciones.notificaciones.dominio.model.TipoNotificacion;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MongoEntity(collection = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionMongoEntity extends PanacheMongoEntity {

    private String eventoId;
    private Long colaboradorId;
    private String destinatarioEmail;
    private String destinatarioNombre;
    private TipoNotificacion tipo;
    private String asunto;
    private String cuerpo;
    private EstadoNotificacion estado;
    private String eventoOrigen;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;
}
