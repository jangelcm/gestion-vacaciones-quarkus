package com.jacm.consultas.api.resource;

import com.jacm.consultas.api.dto.SolicitudConsultaResponse;
import com.jacm.consultas.api.dto.SolicitudHistorialResponse;
import com.jacm.consultas.service.ConsultasProjectionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/v1/consultas")
@Produces(MediaType.APPLICATION_JSON)
public class ConsultaResource {

    @Inject
    ConsultasProjectionService consultasProjectionService;

    @GET
    @Path("/solicitudes/{id}")
    public SolicitudConsultaResponse obtenerSolicitud(@PathParam("id") Long solicitudId) {
        return SolicitudConsultaResponse.fromDocument(consultasProjectionService.obtenerSolicitud(solicitudId));
    }

    @GET
    @Path("/solicitudes/usuario/{colaboradorId}")
    public List<SolicitudConsultaResponse> listarSolicitudesPorUsuario(@PathParam("colaboradorId") String colaboradorId) {
        return consultasProjectionService.listarSolicitudesPorColaborador(colaboradorId)
                .stream()
                .map(SolicitudConsultaResponse::fromDocument)
                .toList();
    }

    @GET
    @Path("/historial/{solicitudId}")
    public List<SolicitudHistorialResponse> listarHistorial(@PathParam("solicitudId") Long solicitudId) {
        return consultasProjectionService.listarHistorial(solicitudId)
                .stream()
                .map(SolicitudHistorialResponse::fromDocument)
                .toList();
    }
}
