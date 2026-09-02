package com.jacm.solicitudes.infrastructure.client;

import com.jacm.solicitudes.infrastructure.client.dto.ValidarSolicitudRequest;
import com.jacm.solicitudes.infrastructure.client.dto.ValidarSolicitudResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v1/politicas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "politicas-client")
public interface PoliticasClient {

    @POST
    @Path("/validar")
    ValidarSolicitudResponse validarSolicitud(ValidarSolicitudRequest request);
}
