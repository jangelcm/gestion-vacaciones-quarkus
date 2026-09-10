package com.jacm.consultas.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.bson.codecs.pojo.annotations.BsonId;

@MongoEntity(collection = "saldos_vacacionales_read")
public class SaldoVacacionalReadDocument {

    @BsonId
    public Long colaboradorId;

    public Long politicaId;
    public LocalDate fechaInicioPolitica;

    public BigDecimal diasDisponibles;
    public BigDecimal diasGozados;
    public BigDecimal diasHabilitados;
    public BigDecimal saldoActual;
    public BigDecimal diasTruncos;
    public Integer diasTrabajados;
    public BigDecimal diasPendientes;
    public BigDecimal diasAcumulados;
    public LocalDate fechaIngresoColaborador;

    public String motivoActualizacion;
    public LocalDateTime ultimaActualizacion;
}
