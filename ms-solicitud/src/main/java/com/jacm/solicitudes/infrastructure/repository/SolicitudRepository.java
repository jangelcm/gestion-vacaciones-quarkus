package com.jacm.solicitudes.infrastructure.repository;

import com.jacm.solicitudes.domain.model.SolicitudEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SolicitudRepository implements PanacheRepository<SolicitudEntity> {
}
