package com.jacm.reportes.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/api/v1/reportes")
@Produces(MediaType.APPLICATION_JSON)
public class ReportesResource {

    @GET
    @Path("/resumen")
    public Map<String, Object> resumen() {
        return Map.of(
                "pendientes", 0,
                "aprobadas", 0,
                "rechazadas", 0,
                "canceladas", 0,
                "mensaje", "ms-reportes inicializado. Pendiente integrar agregaciones reales desde MongoDB.");
    }
}
