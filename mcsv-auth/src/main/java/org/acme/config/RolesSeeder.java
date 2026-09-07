package org.acme.config;

import java.util.List;

import org.acme.models.Rols;
import org.acme.repository.RolsRepository;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class RolesSeeder {

    private static final Logger LOG = Logger.getLogger(RolesSeeder.class);

    static final List<String> ROLES_POR_DEFECTO = List.of(
            "Colaborador",
            "Jefe Inmediato",
            "Recursos Humanos",
            "Administrador");

    private final RolsRepository rolsRepository;

    public RolesSeeder(RolsRepository rolsRepository) {
        this.rolsRepository = rolsRepository;
    }

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (rolsRepository.count() > 0) {
            LOG.info("Roles ya existen, se omite el seed");
            return;
        }

        LOG.info("Cargando roles por defecto: " + ROLES_POR_DEFECTO);

        for (String descripcion : ROLES_POR_DEFECTO) {
            Rols rol = new Rols();
            rol.descripcion = descripcion;
            rolsRepository.persist(rol);
        }
    }
}
