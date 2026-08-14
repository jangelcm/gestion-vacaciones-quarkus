package com.jacm.solicitudes.domain.service;

import com.jacm.solicitudes.api.dto.Solicitud;
import com.jacm.solicitudes.domain.model.EstadoSolicitud;

import java.util.List;

public interface SolicitudService {

     Solicitud crearSolicitud(Solicitud solicitud);
     Solicitud obtenerPorId(Long id);

     List<Solicitud> listarPorColaborador(Long colaboradorId);

     void actualizarEstado(Long id, EstadoSolicitud nuevoEstadoo);
}
