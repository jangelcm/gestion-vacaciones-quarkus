package org.acme.view;

import io.quarkus.runtime.annotations.RegisterForReflection;

// Instanciada dinamicamente por Hibernate via "SELECT new org.acme.view.UserRolView(...)" en
// RolUserRepository; el nombre de la clase solo aparece como texto dentro del JPQL, asi que
// Quarkus no la detecta en build time y GraalVM no la registra para reflexion sin esto.
@RegisterForReflection
public record UserRolView(
    Long userId,
    String username,
    Long rolId,
    String rolName
) {

}
