package com.vacaciones.politicas.res;

import com.vacaciones.politicas.service.SaldoDiasService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/politicas")
@Produces(MediaType.APPLICATION_JSON)
public class SaldoDiasResource {

    private final SaldoDiasService saldoDiasService;

    public SaldoDiasResource(SaldoDiasService saldoDiasService) {
        this.saldoDiasService = saldoDiasService;
    }

    @GET
    @Path("/saldo/{colaboradorId}")
    public Response getByColaboradorId(@PathParam("colaboradorId") Long colaboradorId) {
        return Response.ok(saldoDiasService.getByColaboradorId(colaboradorId)).build();
    }

    @POST
    @Path("/{politicaId}/colaboradores/{colaboradorId}")
    public Response asignarPolitica(
            @PathParam("politicaId") Long politicaId,
            @PathParam("colaboradorId") Long colaboradorId) {
        saldoDiasService.asignarPolitica(colaboradorId, politicaId);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{politicaId}/colaboradores")
    public Response getByPoliticaId(@PathParam("politicaId") Long politicaId) {
        return Response.ok(saldoDiasService.getByPoliticaId(politicaId)).build();
    }
}