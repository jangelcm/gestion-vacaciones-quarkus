package com.vacaciones.notificaciones.infraestructura.adaptadores.out.client;

import com.vacaciones.notificaciones.dominio.model.UsuarioInfo;
import com.vacaciones.notificaciones.dominio.port.out.ResolverUsuarioPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Optional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UsuarioClientAdapter implements ResolverUsuarioPort {

    private static final Logger LOG = Logger.getLogger(UsuarioClientAdapter.class);

    @RestClient
    UsuariosClient usuariosClient;

    @Override
    public Optional<UsuarioInfo> resolverPorColaboradorId(Long colaboradorId) {
        try {
            var usuario = usuariosClient.obtenerPorId(colaboradorId);
            return Optional.of(new UsuarioInfo(usuario.id(), usuario.email(), usuario.username()));
        } catch (WebApplicationException e) {
            LOG.warnf("No se pudo resolver el usuario %d en mcsv-auth (HTTP %d)",
                    colaboradorId, e.getResponse() != null ? e.getResponse().getStatus() : -1);
            return Optional.empty();
        } catch (RuntimeException e) {
            LOG.warnf(e, "Error al resolver el usuario %d en mcsv-auth", colaboradorId);
            return Optional.empty();
        }
    }

    @Override
    public List<UsuarioInfo> resolverPorRol(String rol) {
        try {
            return usuariosClient.listarPorRol(rol).stream()
                    .map(usuario -> new UsuarioInfo(usuario.id(), usuario.email(), usuario.username()))
                    .toList();
        } catch (RuntimeException e) {
            LOG.warnf(e, "No se pudieron resolver los usuarios con rol %s en mcsv-auth", rol);
            return List.of();
        }
    }
}
