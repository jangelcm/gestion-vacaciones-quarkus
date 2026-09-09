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

    public List<SaldoDiasEntity> findAniversariosAcumulables(int mes, int dia) {
        return find("politica.acumulable = true"
                + " and extract(month from createdAt) = ?1"
                + " and extract(day from createdAt) = ?2", mes, dia).list();
    }

    public List<SaldoDiasEntity> findParaProcesoDiario() {
        return listAll();
    }
}