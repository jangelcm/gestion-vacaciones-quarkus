package com.jacm.reportes.service;

import com.jacm.reportes.api.dto.ResumenReporteResponse;
import com.jacm.reportes.model.SolicitudReadDocument;
import com.jacm.reportes.repository.SolicitudReadRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ReporteResumenService {

    private final SolicitudReadRepository repository;

    public ReporteResumenService(SolicitudReadRepository repository) {
        this.repository = repository;
    }

    public ResumenReporteResponse resumenPorEstado(LocalDate desde, LocalDate hasta) {
        List<SolicitudReadDocument> solicitudes = repository.listar(desde, hasta);

        long pendientes = contar(solicitudes, "PENDIENTE");
        long aprobadas = contar(solicitudes, "APROBADA");
        long rechazadas = contar(solicitudes, "RECHAZADA");
        long canceladas = contar(solicitudes, "CANCELADA");

        return new ResumenReporteResponse(pendientes, aprobadas, rechazadas, canceladas, solicitudes.size());
    }

    private long contar(List<SolicitudReadDocument> solicitudes, String estado) {
        return solicitudes.stream().filter(s -> estado.equals(s.estado)).count();
    }
}
