package com.vacaciones.politicas.res;

import com.vacaciones.politicas.service.SaldoDiasService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/politicas/saldo")
@Produces(MediaType.APPLICATION_JSON)
public class SaldoDiasResource {

    private final SaldoDiasService saldoDiasService;

    public SaldoDiasResource(SaldoDiasService saldoDiasService) {
        this.saldoDiasService = saldoDiasService;
    }

    @GET
    @Path("/{colaboradorId}")
    public Response getByColaboradorId(@PathParam("colaboradorId") Long colaboradorId) {
        return Response.ok(saldoDiasService.getByColaboradorId(colaboradorId)).build();
    }
}