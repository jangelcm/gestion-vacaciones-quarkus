package com.jacm.consultas.repository;

import com.jacm.consultas.model.SaldoVacacionalReadDocument;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SaldoVacacionalReadRepository implements PanacheMongoRepositoryBase<SaldoVacacionalReadDocument, Long> {
}
