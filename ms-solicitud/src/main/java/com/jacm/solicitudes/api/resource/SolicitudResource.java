package com.jacm.solicitudes.api.resource;


import com.jacm.solicitudes.api.dto.Solicitud;
import com.jacm.solicitudes.api.dto.SolicitudCrearRequest;
import com.jacm.solicitudes.api.dto.SolicitudResponse;
import com.jacm.solicitudes.domain.service.SolicitudService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/api/v1/solicitudes")
public class SolicitudResource {

    @Inject
    SolicitudService solicitudService;

    @POST
    public Response crearSolicitud(@Valid SolicitudCrearRequest solicitudRequest){
        var nuevaSolicitud = new Solicitud(null,solicitudRequest.colaboradorId(),solicitudRequest.fechaInicio(),solicitudRequest.fechaFin(),null,null);

        Solicitud solicitud = solicitudService.crearSolicitud(nuevaSolicitud);
        return Response.status(Response.Status.CREATED).entity(SolicitudResponse.fromDomain(solicitud)).build();
    }

    @GET
    @Path("/{id}")
    public Response obtenerPorId(@PathParam("id") Long id){
        var solicitud = solicitudService.obtenerPorId(id);
        return Response.status(Response.Status.OK).entity(solicitud).build();
    }

    @GET
    @Path("/usuario/{colaboradorId}")
    public List<SolicitudResponse> listarSolicitudesPorUsuario(@PathParam("colaboradorId") Long colaboradorId){
        return  solicitudService.listarPorColaborador(colaboradorId).stream()
                .map(SolicitudResponse::fromDomain).collect(Collectors.toList());
    }
}
