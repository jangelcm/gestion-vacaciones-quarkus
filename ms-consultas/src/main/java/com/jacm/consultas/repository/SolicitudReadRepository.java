package com.jacm.consultas.repository;

import com.jacm.consultas.model.SolicitudReadDocument;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class SolicitudReadRepository implements PanacheMongoRepositoryBase<SolicitudReadDocument, Long> {

    public List<SolicitudReadDocument> listarPorColaboradorId(String colaboradorId) {
        return find("colaboradorId", Sort.descending("fechaSolicitud"), colaboradorId).list();
    }
}
