package org.acme.repository;

import java.util.List;

import org.acme.models.Rols;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class RolsRepository implements PanacheRepositoryBase<Rols, Long> {

    @Inject
    EntityManager entityManager;

    public List<Rols> findByIdUser(Long idUser) {
        return entityManager.createQuery(
                "select ru.rol from RolsUser ru where ru.user.id = :idUser",
                Rols.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }
}