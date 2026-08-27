package com.jacm.aprobaciones.infrastructure.adapters.in.rest;

import com.jacm.aprobaciones.domain.ports.in.AprobarSolicitudUseCase;
import com.jacm.aprobaciones.domain.ports.in.RechazarSolicitudUseCase;
import com.jacm.aprobaciones.domain.ports.out.AprobacionRepositoryPort;
import com.jacm.aprobaciones.infrastructure.adapters.in.rest.dto.AprobarSolicitudRequest;
import com.jacm.aprobaciones.infrastructure.adapters.in.rest.dto.AprobacionResponse;
import com.jacm.aprobaciones.infrastructure.adapters.in.rest.dto.RechazarSolicitudRequest;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/v1/aprobaciones")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AprobacionResource {

    @Inject
    AprobarSolicitudUseCase aprobarUseCase;

    @Inject
    RechazarSolicitudUseCase rechazarUseCase;

    @Inject
    AprobacionRepositoryPort repositoryPort;

    /** Lista todas las aprobaciones en estado PENDIENTE. */
    @GET
    public List<AprobacionResponse> listarPendientes() {
        return repositoryPort.listarPendientes()
                .stream()
                .map(AprobacionResponse::fromDomain)
                .toList();
    }

    /** Obtiene una aprobación por su ID. */
    @GET
    @Path("/{id}")
    public Response obtenerPorId(@PathParam("id") Long id) {
        return repositoryPort.buscarPorId(id)
                .map(AprobacionResponse::fromDomain)
                .map(r -> Response.ok(r).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    /** Aprueba una solicitud de vacaciones. */
    @POST
    @Path("/{solicitudId}/aprobar")
    public Response aprobar(
            @PathParam("solicitudId") Long solicitudId,
            @Valid AprobarSolicitudRequest request) {

        var aprobacion = aprobarUseCase.aprobar(solicitudId, request.aprobadorId(), request.comentario());
        return Response.ok(AprobacionResponse.fromDomain(aprobacion)).build();
    }

    /** Rechaza una solicitud de vacaciones. */
    @POST
    @Path("/{solicitudId}/rechazar")
    public Response rechazar(
            @PathParam("solicitudId") Long solicitudId,
            @Valid RechazarSolicitudRequest request) {

        var aprobacion = rechazarUseCase.rechazar(solicitudId, request.aprobadorId(), request.motivo());
        return Response.ok(AprobacionResponse.fromDomain(aprobacion)).build();
    }
}
