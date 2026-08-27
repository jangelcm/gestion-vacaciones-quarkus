package com.jacm.solicitudes.domain.ports.in;

import com.jacm.solicitudes.api.dto.Solicitud;
import com.jacm.solicitudes.domain.model.EstadoSolicitud;

import java.util.List;

/**
 * Puerto de entrada: casos de uso del microservicio de solicitudes.
 * Define el contrato con el que la infraestructura (REST, Kafka) interactúa
 * con la capa de aplicación.
 */
public interface SolicitudUseCase {

    Solicitud crearSolicitud(Solicitud solicitud);

    Solicitud obtenerPorId(Long id);

    List<Solicitud> listarPorColaborador(Long colaboradorId);

    void actualizarEstado(Long id, EstadoSolicitud nuevoEstado);
}
