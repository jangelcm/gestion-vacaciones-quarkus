package com.jacm.consultas.api.dto;

import com.jacm.consultas.model.PoliticaReadDocument;

public record PoliticaConsultaResponse(
        Long id,
        String nombre,
        String tipoVacacion,
        Integer diasBaseAnio,
        Integer antiguedadMinimaMeses,
        Boolean acumulable,
        Integer maxDiasAcumulables,
        Boolean activa) {

    public static PoliticaConsultaResponse fromDocument(PoliticaReadDocument doc) {
        return new PoliticaConsultaResponse(
                doc.politicaId,
                doc.nombre,
                doc.tipoVacacion,
                doc.diasBaseAnio,
                doc.antiguedadMinimaMeses,
                doc.acumulable,
                doc.maxDiasAcumulables,
                doc.activa);
    }
}
