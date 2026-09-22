package com.jacm.consultas.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.bson.codecs.pojo.annotations.BsonId;

// @RegisterForReflection: se serializa con Jackson "a mano" en ConsultaUpdatesNotifier
// para el push por WebSocket (ver SolicitudReadDocument para el detalle del porque).
@RegisterForReflection
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
