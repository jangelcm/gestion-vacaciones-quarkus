package com.jacm.reportes.api;

import com.jacm.reportes.api.dto.ResumenReporteResponse;
import com.jacm.reportes.api.dto.SaldoReporteResponse;
import com.jacm.reportes.service.ReporteResumenService;
import com.jacm.reportes.service.ReporteSaldoService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;

@Path("/api/v1/reportes")
@Produces(MediaType.APPLICATION_JSON)
public class ReportesResource {

    private final ReporteResumenService resumenService;
    private final ReporteSaldoService saldoService;

    public ReportesResource(ReporteResumenService resumenService, ReporteSaldoService saldoService) {
        this.resumenService = resumenService;
        this.saldoService = saldoService;
    }

    @GET
    @Path("/resumen")
    public ResumenReporteResponse resumen(@QueryParam("desde") String desde, @QueryParam("hasta") String hasta) {
        return resumenService.resumenPorEstado(parseONull(desde), parseONull(hasta));
    }

    @GET
    @Path("/saldos")
    public List<SaldoReporteResponse> saldos(@QueryParam("politicaId") Long politicaId) {
        return saldoService.listarPorColaborador(politicaId);
    }

    private LocalDate parseONull(String valor) {
        return valor == null || valor.isBlank() ? null : LocalDate.parse(valor);
    }
}
