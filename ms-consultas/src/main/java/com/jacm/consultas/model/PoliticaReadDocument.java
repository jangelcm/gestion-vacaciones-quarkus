package com.jacm.consultas.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import java.time.LocalDateTime;
import org.bson.codecs.pojo.annotations.BsonId;

@MongoEntity(collection = "politicas_read")
public class PoliticaReadDocument {

    @BsonId
    public Long politicaId;

    public String nombre;
    public String tipoVacacion;
    public Integer diasBaseAnio;
    public Integer antiguedadMinimaMeses;
    public Boolean acumulable;
    public Integer maxDiasAcumulables;
    public Boolean activa;
    public LocalDateTime ultimaActualizacion;
}
