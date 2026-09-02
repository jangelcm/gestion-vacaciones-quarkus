package com.jacm.solicitudes.domain.service;


import com.jacm.solicitudes.api.dto.Solicitud;
import com.jacm.solicitudes.domain.exception.DependenciaNoDisponibleException;
import com.jacm.solicitudes.domain.exception.SolicitudNoValidaException;
import com.jacm.solicitudes.domain.model.EstadoSolicitud;
import com.jacm.solicitudes.domain.ports.out.SolicitudEventPublisherPort;
import com.jacm.solicitudes.domain.ports.out.SolicitudRepositoryPort;
import com.jacm.solicitudes.infrastructure.client.PoliticasClient;
import com.jacm.solicitudes.infrastructure.client.dto.ValidarSolicitudRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
@Transactional
public class SolicitudServiceImpl implements SolicitudService {

    private static final String DEFAULT_TIPO_VACACION = "ANUAL";
    private static final Integer DEFAULT_ANTIGUEDAD_MESES = 12;
    private static final String MENSAJE_VALIDACION_DEFAULT = "La solicitud no cumple las politicas vigentes";
    private static final Pattern MENSAJE_ERROR_PATTERN = Pattern.compile("\"mensaje\"\\s*:\\s*\"([^\"]+)\"");

    @Inject
    SolicitudRepositoryPort solicitudRepositoryPort;

    @Inject
    SolicitudEventPublisherPort solicitudEventPublisherPort;

    @Inject
    @RestClient
    PoliticasClient politicasClient;

    @Override
    public Solicitud crearSolicitud(Solicitud solicitud) {
        /*TODO:Validación pendiente de solucionar por error de saldos */
        validarSolicitudConPoliticas(solicitud);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        var solicitudGuardada = solicitudRepositoryPort.guardar(solicitud);
        solicitudEventPublisherPort.publicarSolicitudCreada(solicitudGuardada);
        return solicitudGuardada;
    }

    @Override
    public Solicitud obtenerPorId(Long id) {
        return solicitudRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada con id: " + id));
    }

    @Override
    public List<Solicitud> listarPorColaborador(Long colaboradorId) {
        return solicitudRepositoryPort.listarPorColaboradorId(String.valueOf(colaboradorId));
    }

    @Override
    public void actualizarEstado(Long id, EstadoSolicitud nuevoEstado) {
        solicitudRepositoryPort.actualizarEstado(id, nuevoEstado);
    }

    private void validarSolicitudConPoliticas(Solicitud solicitud) {
        Long colaboradorId = Long.valueOf(solicitud.getColaboradorId());
        ValidarSolicitudRequest request = new ValidarSolicitudRequest(
                colaboradorId,
                solicitud.getFechaInicio(),
                solicitud.getFechaFin(),
                DEFAULT_TIPO_VACACION,
                DEFAULT_ANTIGUEDAD_MESES);

        try {
            var respuesta = politicasClient.validarSolicitud(request);
            if (respuesta == null || !respuesta.aprobado()) {
                String motivo = respuesta != null && respuesta.motivoRechazo() != null
                        ? respuesta.motivoRechazo()
                        : MENSAJE_VALIDACION_DEFAULT;
                throw new SolicitudNoValidaException(motivo);
            }
        } catch (SolicitudNoValidaException e) {
            throw e;
        } catch (WebApplicationException e) {
            throw mapearErrorPoliticas(e);
        } catch (ProcessingException e) {
            throw new DependenciaNoDisponibleException(
                    "No se pudo validar la solicitud con el servicio de politicas. Intente nuevamente.");
        }
    }

    private RuntimeException mapearErrorPoliticas(WebApplicationException e) {
        Response response = e.getResponse();
        int status = response != null ? response.getStatus() : 0;

        if (status >= 400 && status < 500) {
            return new SolicitudNoValidaException(extraerMensajeError(response));
        }

        return new DependenciaNoDisponibleException(
                "No se pudo validar la solicitud con el servicio de politicas. Intente nuevamente.");
    }

    private String extraerMensajeError(Response response) {
        if (response == null) {
            return MENSAJE_VALIDACION_DEFAULT;
        }

        try {
            String body = response.readEntity(String.class);
            if (body == null || body.isBlank()) {
                return MENSAJE_VALIDACION_DEFAULT;
            }

            Matcher matcher = MENSAJE_ERROR_PATTERN.matcher(body);
            if (matcher.find()) {
                return matcher.group(1);
            }

            return body;
        } catch (Exception ignored) {
            return MENSAJE_VALIDACION_DEFAULT;
        }
    }


}

