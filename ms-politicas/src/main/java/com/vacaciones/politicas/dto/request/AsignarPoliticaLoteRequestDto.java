package com.vacaciones.politicas.dto.request;

import java.time.LocalDate;
import java.util.List;

public record AsignarPoliticaLoteRequestDto(
        LocalDate fechaInicioPolitica,
        List<ColaboradorAsignacion> colaboradores) {

    public record ColaboradorAsignacion(Long colaboradorId, LocalDate fechaIngresoColaborador) {
    }
}
