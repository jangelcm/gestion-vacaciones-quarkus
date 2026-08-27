package org.acme.services;

import java.util.List;

import org.acme.models.Rols;
import org.acme.repository.RolsRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RolsService {

    @Inject
    RolsRepository rolsRepository;

    public List<Rols> getAll() {
        return rolsRepository.listAll();
    }

    public List<Rols> getByIdUser(Long idUser) {
        return rolsRepository.findByIdUser(idUser);
    }

    @Transactional
    public Rols create(Rols rol) {
        rolsRepository.persist(rol);
        return rol;
    }
}