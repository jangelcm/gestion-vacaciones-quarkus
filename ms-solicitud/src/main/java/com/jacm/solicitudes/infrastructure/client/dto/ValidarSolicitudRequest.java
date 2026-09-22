package com.jacm.solicitudes.infrastructure.client.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDate;

// Serializado por el REST client al llamar a ms-politicas; necesita reflexion Jackson
// para que el cliente lo convierta a JSON en imagen nativa.
@RegisterForReflection
public record ValidarSolicitudRequest(
        Long colaboradorId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String tipoVacacion,
        Integer antiguedadMeses) {
}
