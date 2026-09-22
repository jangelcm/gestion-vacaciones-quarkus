package org.acme.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDate;

// Se devuelve envuelta en un Response generico desde AuthResource#register (no como tipo
// de retorno directo), asi que Quarkus no la detecta sola para incluir su reflexion Jackson
// en imagen nativa (la reflexion de Hibernate para @Entity es un registro aparte).
@RegisterForReflection
@Entity
@Table(name = "users")
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true, nullable = false)
    public String username;

    @Column(nullable = false)
    public String passwordHash;

    @Column
    public String refreshToken;

    @Column(name = "is_active")
    public boolean isActive;

    @Column
    public String email;

    @Column
    public String telefono;

    @Column(name = "fecha_ingreso")
    public LocalDate fechaIngreso;
    

}
