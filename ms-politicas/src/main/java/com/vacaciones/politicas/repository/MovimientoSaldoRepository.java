package com.vacaciones.politicas.repository;

import com.vacaciones.politicas.entity.MovimientoSaldoEntity;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MovimientoSaldoRepository implements PanacheRepositoryBase<MovimientoSaldoEntity, Long> {

    public boolean existsByEventoId(String eventoId) {
        return count("eventoId", eventoId) > 0;
    }
}