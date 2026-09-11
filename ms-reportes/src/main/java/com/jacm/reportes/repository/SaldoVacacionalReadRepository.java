package com.jacm.reportes.repository;

import com.jacm.reportes.model.SaldoVacacionalReadDocument;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class SaldoVacacionalReadRepository implements PanacheMongoRepositoryBase<SaldoVacacionalReadDocument, Long> {

    public List<SaldoVacacionalReadDocument> listar(Long politicaId) {
        if (politicaId != null) {
            return find("politicaId", Sort.ascending("colaboradorId"), politicaId).list();
        }
        return listAll(Sort.ascending("colaboradorId"));
    }
}
