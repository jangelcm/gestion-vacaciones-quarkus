package com.vacaciones.politicas.res;

import com.vacaciones.politicas.dto.request.PoliticaRequestDto;
import com.vacaciones.politicas.service.PoliticaService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/politicas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PoliticaResource {

    private final PoliticaService politicaService;

    public PoliticaResource(PoliticaService politicaService) {
        this.politicaService = politicaService;
    }

    @POST
    public Response save(PoliticaRequestDto request) {
        return Response.status(Response.Status.CREATED)
                .entity(politicaService.save(request))
                .build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        return Response.ok(politicaService.findById(id)).build();
    }

    @GET
    public Response getAll() {
        return Response.ok(politicaService.getAll()).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, PoliticaRequestDto request) {
        return Response.ok(politicaService.update(id, request)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        return Response.ok(politicaService.delete(id)).build();
    }
}