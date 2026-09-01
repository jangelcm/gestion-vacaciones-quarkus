package com.vacaciones.notificaciones.infraestructura.adaptadores.in.rest;

import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.port.in.ConsultarHistorialUseCase;
import com.vacaciones.notificaciones.dominio.port.in.EnviarNotificacionUseCase;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.rest.dto.request.NotificacionTestRequestDto;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.rest.dto.response.NotificacionResponseDto;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.rest.mapper.NotificacionRestMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/notificaciones")
public class NotificacionResource {

    private final EnviarNotificacionUseCase enviarNotificacionUseCase;
    private final ConsultarHistorialUseCase consultarHistorialUseCase;
    private final NotificacionRestMapper mapper;

    public NotificacionResource(
            EnviarNotificacionUseCase enviarNotificacionUseCase,
            ConsultarHistorialUseCase consultarHistorialUseCase,
            NotificacionRestMapper mapper) {
        this.enviarNotificacionUseCase = enviarNotificacionUseCase;
        this.consultarHistorialUseCase = consultarHistorialUseCase;
        this.mapper = mapper;
    }

    // TODO: restringir a rol ADMIN (@RolesAllowed) cuando se configure seguridad (JWT/OIDC) en este microservicio.
    @POST
    @Path("/test")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response enviarNotificacionDePrueba(NotificacionTestRequestDto request) {
        Notificacion notificacion = mapper.toDominio(request);
        enviarNotificacionUseCase.enviar(notificacion);
        return Response.status(Response.Status.CREATED).entity(mapper.toResponseDto(notificacion)).build();
    }

    @GET
    @Path("/historial/{colaboradorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<NotificacionResponseDto> historial(@PathParam("colaboradorId") Long colaboradorId) {
        return consultarHistorialUseCase.consultarPorColaborador(colaboradorId).stream()
                .map(mapper::toResponseDto)
                .toList();
    }
}
