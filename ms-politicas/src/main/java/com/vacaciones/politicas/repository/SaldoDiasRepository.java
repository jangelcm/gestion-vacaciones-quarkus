package com.vacaciones.politicas.repository;

import com.vacaciones.politicas.entity.SaldoDiasEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SaldoDiasRepository implements PanacheRepositoryBase<SaldoDiasEntity, Long> {

    public SaldoDiasEntity findByColaboradorId(Long colaboradorId) {
        return find("colaboradorId", colaboradorId).firstResult();
    }
}