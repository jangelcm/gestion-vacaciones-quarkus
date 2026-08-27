package org.acme.repository;

import java.util.List;

import org.acme.models.RolsUser;
import org.acme.view.UserRolView;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import jakarta.enterprise.context.ApplicationScoped;



@ApplicationScoped
public class RolUserRepository implements PanacheRepository<RolsUser> {



    public RolsUser findByRolIdAndUserId(Long rolId, Long userId) {
        return find("from RolsUser ru where ru.rol.id = ?1 and ru.user.id = ?2", rolId, userId).firstResult();
    }

    public List<RolsUser> findByUserId(Long userId) {
        return list("from RolsUser ru where ru.user.id = ?1", userId);
    }

    public RolsUser findByUserIdAndRolId(Long userId, Long rolId) {
        return find("from RolsUser ru where ru.user.id = :userId and ru.rol.id = :rolId", 
                userId, rolId)  
                .firstResult();
    }

    public List<UserRolView> findUserRolViews(Long userId) {


        return getEntityManager().createQuery(
                "SELECT new org.acme.view.UserRolView(u.id, u.username, r.idRol, r.descripcion) " +
                "FROM RolsUser ru " +
                "JOIN ru.user u " +
                "JOIN ru.rol r " +
                "WHERE u.id = :userId",
                UserRolView.class)
            .setParameter("userId", userId)
            .getResultList();
    }

}
