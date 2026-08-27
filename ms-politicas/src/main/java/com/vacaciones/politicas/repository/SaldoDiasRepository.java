package com.vacaciones.politicas.repository;

import com.vacaciones.politicas.entity.SaldoDiasEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class SaldoDiasRepository implements PanacheRepositoryBase<SaldoDiasEntity, Long> {

    public SaldoDiasEntity findByColaboradorId(Long colaboradorId) {
        return find("colaboradorId", colaboradorId).firstResult();
    }

    public List<SaldoDiasEntity> findByPoliticaId(Long politicaId) {
        return find("politica.id", politicaId).list();
    }
}