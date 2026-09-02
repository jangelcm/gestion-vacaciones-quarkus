package com.jacm.consultas.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.bson.codecs.pojo.annotations.BsonId;

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
