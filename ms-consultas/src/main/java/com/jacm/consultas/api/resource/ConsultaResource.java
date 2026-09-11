package com.jacm.consultas.api.resource;

import com.jacm.consultas.api.dto.SolicitudConsultaResponse;
import com.jacm.consultas.api.dto.SolicitudHistorialResponse;
import com.jacm.consultas.api.dto.SaldoVacacionalResponse;
import com.jacm.consultas.api.dto.PoliticaConsultaResponse;
import com.jacm.consultas.service.ConsultasProjectionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
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

    /** Solicitudes de todos los colaboradores que se solapan con [desde, hasta] — para el calendario de equipo. */
    @GET
    @Path("/solicitudes")
    public List<SolicitudConsultaResponse> listarSolicitudesEnRango(
            @QueryParam("desde") String desde, @QueryParam("hasta") String hasta) {
        return consultasProjectionService.listarSolicitudesEnRango(LocalDate.parse(desde), LocalDate.parse(hasta))
                .stream()
                .map(SolicitudConsultaResponse::fromDocument)
                .toList();
    }

    /** Solicitudes PENDIENTE de todos los colaboradores — para la pantalla de aprobaciones. */
    @GET
    @Path("/solicitudes/pendientes")
    public List<SolicitudConsultaResponse> listarSolicitudesPendientes() {
        return consultasProjectionService.listarSolicitudesPendientes()
                .stream()
                .map(SolicitudConsultaResponse::fromDocument)
                .toList();
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

    @GET
    @Path("/balances/{colaboradorId}")
    public SaldoVacacionalResponse obtenerBalance(@PathParam("colaboradorId") Long colaboradorId) {
        return SaldoVacacionalResponse.fromDocument(consultasProjectionService.obtenerSaldoVacacional(colaboradorId));
    }

    @GET
    @Path("/politicas")
    public List<PoliticaConsultaResponse> listarPoliticas() {
        return consultasProjectionService.listarPoliticas()
                .stream()
                .map(PoliticaConsultaResponse::fromDocument)
                .toList();
    }
}
