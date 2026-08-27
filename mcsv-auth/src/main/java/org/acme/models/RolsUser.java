package org.acme.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rols_user")
public class RolsUser {

    @Id
    @Column(name = "id_rol_user")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idRolUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    public Rols rol;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    public User user;
}