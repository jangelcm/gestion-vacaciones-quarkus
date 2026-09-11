package com.jacm.reportes.service;

import com.jacm.reportes.api.dto.SaldoReporteResponse;
import com.jacm.reportes.model.PoliticaReadDocument;
import com.jacm.reportes.model.SaldoVacacionalReadDocument;
import com.jacm.reportes.repository.PoliticaReadRepository;
import com.jacm.reportes.repository.SaldoVacacionalReadRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ReporteSaldoService {

    private final SaldoVacacionalReadRepository saldoRepository;
    private final PoliticaReadRepository politicaRepository;

    public ReporteSaldoService(SaldoVacacionalReadRepository saldoRepository, PoliticaReadRepository politicaRepository) {
        this.saldoRepository = saldoRepository;
        this.politicaRepository = politicaRepository;
    }

    public List<SaldoReporteResponse> listarPorColaborador(Long politicaId) {
        List<SaldoVacacionalReadDocument> saldos = saldoRepository.listar(politicaId);
        Map<Long, String> nombresPorPolitica = new HashMap<>();

        return saldos.stream()
                .map(s -> new SaldoReporteResponse(
                        s.colaboradorId,
                        s.politicaId,
                        s.politicaId == null ? null : nombresPorPolitica.computeIfAbsent(s.politicaId, this::nombrePolitica),
                        s.diasDisponibles,
                        s.diasGozados,
                        s.diasHabilitados,
                        s.saldoActual,
                        s.diasAcumulados,
                        s.diasPendientes))
                .toList();
    }

    private String nombrePolitica(Long politicaId) {
        PoliticaReadDocument politica = politicaRepository.findById(politicaId);
        return politica == null ? null : politica.nombre;
    }
}
