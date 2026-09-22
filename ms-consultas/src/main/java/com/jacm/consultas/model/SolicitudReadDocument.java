package com.jacm.consultas.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.bson.codecs.pojo.annotations.BsonId;

// @RegisterForReflection: ademas de persistirse en Mongo, esta clase se serializa con
// Jackson "a mano" en ConsultaUpdatesNotifier para el push por WebSocket — ese uso no
// pasa por un endpoint REST ni por Panache-Mongo, asi que en imagen nativa GraalVM no
// genera su metadata de reflexion salvo que se pida explicito.
@RegisterForReflection
@MongoEntity(collection = "solicitudes_read")
public class SolicitudReadDocument {

    @BsonId
    public Long solicitudId;

    public String colaboradorId;
    public LocalDate fechaInicio;
    public LocalDate fechaFin;
    public LocalDate fechaSolicitud;
    public String estado;
    public LocalDateTime ultimaActualizacion;
}
