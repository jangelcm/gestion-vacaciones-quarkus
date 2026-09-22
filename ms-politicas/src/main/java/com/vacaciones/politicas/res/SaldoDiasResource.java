package com.vacaciones.politicas.res;

import com.vacaciones.politicas.dto.request.AsignarPoliticaLoteRequestDto;
import com.vacaciones.politicas.dto.request.AsignarPoliticaRequestDto;
import com.vacaciones.politicas.dto.response.AsignarPoliticaLoteResponseDto;
import com.vacaciones.politicas.service.SaldoDiasService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;

@Path("/api/v1/politicas")
@Produces(MediaType.APPLICATION_JSON)
public class SaldoDiasResource {

    private static final Logger LOG = Logger.getLogger(SaldoDiasResource.class);

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
    @Consumes(MediaType.APPLICATION_JSON)
    public Response asignarPolitica(
            @PathParam("politicaId") Long politicaId,
            @PathParam("colaboradorId") Long colaboradorId,
            AsignarPoliticaRequestDto request) {
        saldoDiasService.asignarPolitica(
            colaboradorId,
            politicaId,
            request != null ? request.fechaInicioPolitica() : null,
            request != null ? request.fechaIngresoColaborador() : null);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{politicaId}/colaboradores")
    public Response getByPoliticaId(@PathParam("politicaId") Long politicaId) {
        return Response.ok(saldoDiasService.getByPoliticaId(politicaId)).build();
    }

    /**
     * Asigna la politica a varios colaboradores en una sola llamada (ej. "todos los del
     * rol X") en vez de que el front dispare una peticion POST por colaborador. Cada
     * asignacion corre en su propia transaccion (via saldoDiasService.asignarPolitica,
     * que es @Transactional): si una falla (ya tenia politica, etc.) no aborta al resto.
     */
    @POST
    @Path("/{politicaId}/colaboradores/lote")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response asignarPoliticaLote(
            @PathParam("politicaId") Long politicaId,
            AsignarPoliticaLoteRequestDto request) {
        int completados = 0;
        List<String> errores = new ArrayList<>();

        for (AsignarPoliticaLoteRequestDto.ColaboradorAsignacion colaborador : request.colaboradores()) {
            try {
                saldoDiasService.asignarOActualizarPolitica(
                        colaborador.colaboradorId(),
                        politicaId,
                        request.fechaInicioPolitica(),
                        colaborador.fechaIngresoColaborador());
                completados++;
            } catch (RuntimeException e) {
                LOG.warnf("No se pudo asignar politica %d a colaborador %d: %s",
                        politicaId, colaborador.colaboradorId(), e.getMessage());
                errores.add("Colaborador " + colaborador.colaboradorId() + ": " + e.getMessage());
            }
        }

        return Response.ok(new AsignarPoliticaLoteResponseDto(completados, errores.size(), errores)).build();
    }
}