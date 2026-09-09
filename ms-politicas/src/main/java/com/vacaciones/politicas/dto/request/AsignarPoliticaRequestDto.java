package com.vacaciones.politicas.dto.request;

import java.time.LocalDate;

public record AsignarPoliticaRequestDto(
        LocalDate fechaInicioPolitica,
        LocalDate fechaIngresoColaborador) {
}
