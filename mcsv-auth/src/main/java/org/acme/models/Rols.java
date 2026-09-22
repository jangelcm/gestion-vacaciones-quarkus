package org.acme.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import io.quarkus.runtime.annotations.RegisterForReflection;

// Se devuelve envuelta en un Response generico desde RolsResource (no como tipo de
// retorno directo), asi que Quarkus no la detecta sola para incluir su reflexion Jackson
// en imagen nativa (la reflexion de Hibernate para @Entity es un registro aparte).
@RegisterForReflection
@Entity
@Table(name = "rols")
public class Rols {

    @Id
    @Column(name = "id_rol")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idRol;

    @Column(nullable = false)
    public String descripcion;
}