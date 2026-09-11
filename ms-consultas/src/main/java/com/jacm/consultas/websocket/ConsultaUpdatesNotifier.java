package com.jacm.consultas.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.websockets.next.OpenConnections;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ConsultaUpdatesNotifier {

    private static final Logger LOG = Logger.getLogger(ConsultaUpdatesNotifier.class);

    private final SesionesConsultasRegistry registro;
    private final ObjectMapper objectMapper;
    private final OpenConnections openConnections;

    public ConsultaUpdatesNotifier(
            SesionesConsultasRegistry registro,
            ObjectMapper objectMapper,
            OpenConnections openConnections) {
        this.registro = registro;
        this.objectMapper = objectMapper;
        this.openConnections = openConnections;
    }

    public void notificar(Long colaboradorId, String tipoEvento, Object payload) {
        if (colaboradorId == null) {
            return;
        }
        Set<String> conexionesIds = registro.obtenerConexiones(colaboradorId);
        if (conexionesIds.isEmpty()) {
            LOG.debugf("Sin conexion websocket activa para el colaborador %d; se ignora el push "
                    + "en tiempo real (la pantalla se actualizara al recargar)", colaboradorId);
            return;
        }

        try {
            String mensaje = serializar(tipoEvento, payload);
            for (String conexionId : conexionesIds) {
                openConnections.findByConnectionId(conexionId)
                        .ifPresent(conexion -> conexion.sendTextAndAwait(mensaje));
            }
        } catch (RuntimeException e) {
            LOG.errorf(e, "Fallo al notificar actualizaciones de consultas al colaborador %d", colaboradorId);
        }
    }

    private String serializar(String tipoEvento, Object payload) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("tipoEvento", tipoEvento);
        cuerpo.put("payload", payload);
        try {
            return objectMapper.writeValueAsString(cuerpo);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar evento de actualizacion de consultas", e);
        }
    }
}
