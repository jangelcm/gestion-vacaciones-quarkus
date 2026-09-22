package com.jacm.aprobaciones.infrastructure.exception;

import io.quarkus.runtime.annotations.RegisterForReflection;

// Se serializa solo desde ExceptionMapper (fuera del camino normal de un endpoint REST
// exitoso), asi que Quarkus no lo detecta solo para incluir su reflexion en imagen nativa.
@RegisterForReflection
public record ErrorResponse(
        String hora,
        String mensaje,
        String url,
        String codeStatus) {
}
