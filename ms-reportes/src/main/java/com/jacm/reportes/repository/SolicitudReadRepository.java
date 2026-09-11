package com.jacm.reportes.repository;

import com.jacm.reportes.model.SolicitudReadDocument;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class SolicitudReadRepository implements PanacheMongoRepositoryBase<SolicitudReadDocument, Long> {

    /** Todas las solicitudes, opcionalmente acotadas por fechaInicio en [desde, hasta]. */
    public List<SolicitudReadDocument> listar(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null) {
            return find("fechaInicio >= ?1 and fechaInicio <= ?2", desde, hasta).list();
        }
        if (desde != null) {
            return find("fechaInicio >= ?1", desde).list();
        }
        if (hasta != null) {
            return find("fechaInicio <= ?1", hasta).list();
        }
        return listAll();
    }
}
