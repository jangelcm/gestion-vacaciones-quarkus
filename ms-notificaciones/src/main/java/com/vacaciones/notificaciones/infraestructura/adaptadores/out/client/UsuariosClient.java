package com.vacaciones.notificaciones.infraestructura.adaptadores.out.client;

import com.vacaciones.notificaciones.infraestructura.adaptadores.out.client.dto.UsuarioResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente REST hacia mcsv-auth, directo contenedor-a-contenedor (sin gateway, sin JWT),
 * igual patron que ms-solicitud -> ms-politicas. Usa GET /users/{id}, expuesto publicamente
 * en mcsv-auth para este uso interno (ver quarkus.http.auth.permission.internal-users-get).
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "usuarios-client")
public interface UsuariosClient {

    @GET
    @Path("/{id}")
    UsuarioResponse obtenerPorId(@PathParam("id") Long id);

    @GET
    @Path("/rol/{rol}")
    List<UsuarioResponse> listarPorRol(@PathParam("rol") String rol);
}
