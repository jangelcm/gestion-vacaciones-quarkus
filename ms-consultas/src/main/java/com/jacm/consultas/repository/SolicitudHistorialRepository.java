package com.jacm.consultas.repository;

import com.jacm.consultas.model.SolicitudHistorialDocument;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class SolicitudHistorialRepository implements PanacheMongoRepository<SolicitudHistorialDocument> {

    public List<SolicitudHistorialDocument> listarPorSolicitudId(Long solicitudId) {
        return find("solicitudId", Sort.ascending("fechaEvento"), solicitudId).list();
    }
}
