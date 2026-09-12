package com.vacaciones.politicas.dto.response;

import java.util.List;

public record AsignarPoliticaLoteResponseDto(
        int completados,
        int errores,
        List<String> mensajesError) {
}
