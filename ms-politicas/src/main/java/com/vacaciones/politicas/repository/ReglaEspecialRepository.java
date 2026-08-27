package com.vacaciones.politicas.repository;

import com.vacaciones.politicas.entity.ReglaEspecialEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReglaEspecialRepository implements PanacheRepositoryBase<ReglaEspecialEntity, Long> {
}