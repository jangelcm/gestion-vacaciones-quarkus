package com.vacaciones.politicas.dto.response;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
public record AsignarPoliticaLoteResponseDto(
        int completados,
        int errores,
        List<String> mensajesError) {
}
