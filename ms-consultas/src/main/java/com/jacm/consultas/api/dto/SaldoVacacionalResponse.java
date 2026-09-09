package com.jacm.consultas.api.dto;

import com.jacm.consultas.model.SaldoVacacionalReadDocument;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SaldoVacacionalResponse(
        Long colaboradorId,
    Long politicaId,
    LocalDate fechaInicioPolitica,
        BigDecimal diasGozados,
        BigDecimal diasHabilitados,
        BigDecimal saldoActual,
        BigDecimal diasTruncos,
    Integer diasTrabajados,
        BigDecimal diasPendientes,
        String motivoActualizacion,
        LocalDateTime ultimaActualizacion) {

    public static SaldoVacacionalResponse fromDocument(SaldoVacacionalReadDocument doc) {
        return new SaldoVacacionalResponse(
                doc.colaboradorId,
            doc.politicaId,
            doc.fechaInicioPolitica,
                doc.diasGozados,
                doc.diasHabilitados,
                doc.saldoActual,
                doc.diasTruncos,
                doc.diasTrabajados,
                doc.diasPendientes,
                doc.motivoActualizacion,
                doc.ultimaActualizacion);
    }
}
