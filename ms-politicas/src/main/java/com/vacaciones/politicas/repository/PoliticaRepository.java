package com.vacaciones.politicas.repository;

import com.vacaciones.politicas.entity.PoliticaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PoliticaRepository implements PanacheRepositoryBase<PoliticaEntity, Long> {

    public PoliticaEntity findByEsPorDefectoTrue() {
        return find("esPorDefecto", true).firstResult();
    }
}