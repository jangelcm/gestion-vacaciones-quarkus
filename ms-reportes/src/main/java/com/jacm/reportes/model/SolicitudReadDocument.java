package com.jacm.reportes.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.bson.codecs.pojo.annotations.BsonId;

/**
 * Espejo de solo lectura de com.jacm.consultas.model.SolicitudReadDocument: ms-reportes
 * lee la misma coleccion "solicitudes_read" en consultas_db, nunca escribe en ella.
 */
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
