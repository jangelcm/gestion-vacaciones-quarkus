package com.jacm.reportes.repository;

import com.jacm.reportes.model.PoliticaReadDocument;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PoliticaReadRepository implements PanacheMongoRepositoryBase<PoliticaReadDocument, Long> {
}
