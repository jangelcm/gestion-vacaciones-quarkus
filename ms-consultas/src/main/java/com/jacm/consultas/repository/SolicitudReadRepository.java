package com.jacm.consultas.repository;

import com.jacm.consultas.model.SolicitudReadDocument;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class SolicitudReadRepository implements PanacheMongoRepositoryBase<SolicitudReadDocument, Long> {

    public List<SolicitudReadDocument> listarPorColaboradorId(String colaboradorId) {
        return find("colaboradorId", Sort.descending("fechaSolicitud"), colaboradorId).list();
    }

    /** Solicitudes cuyo rango [fechaInicio, fechaFin] se solapa con [desde, hasta], de todos los colaboradores. */
    public List<SolicitudReadDocument> listarEnRango(LocalDate desde, LocalDate hasta) {
        return find("fechaInicio <= ?1 and fechaFin >= ?2", Sort.ascending("fechaInicio"), hasta, desde).list();
    }
}
