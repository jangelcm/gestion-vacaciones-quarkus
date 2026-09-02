package com.jacm.consultas.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import java.time.LocalDateTime;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

@MongoEntity(collection = "solicitudes_historial")
public class SolicitudHistorialDocument {

    @BsonId
    public ObjectId id;

    public Long solicitudId;
    public String estado;
    public String detalle;
    public LocalDateTime fechaEvento;
}
