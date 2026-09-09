package com.jacm.consultas.repository;

import com.jacm.consultas.model.PoliticaReadDocument;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PoliticaReadRepository implements PanacheMongoRepositoryBase<PoliticaReadDocument, Long> {
}
