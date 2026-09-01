package com.vacaciones.notificaciones.infraestructura.adaptadores.out.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacaciones.notificaciones.dominio.port.out.NotificadorTiempoRealPort;
import io.quarkus.websockets.next.OpenConnections;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WebSocketNotificadorAdapter implements NotificadorTiempoRealPort {

    private static final Logger LOG = Logger.getLogger(WebSocketNotificadorAdapter.class);

    private final SesionesWebSocketRegistry registro;
    private final ObjectMapper objectMapper;
    private final OpenConnections openConnections;

    public WebSocketNotificadorAdapter(
            SesionesWebSocketRegistry registro, ObjectMapper objectMapper, OpenConnections openConnections) {
        this.registro = registro;
        this.objectMapper = objectMapper;
        this.openConnections = openConnections;
    }

    @Override
    public void notificar(Long colaboradorId, String tipoEvento, Object payload) {
        Set<String> conexionesIds = registro.obtenerConexiones(colaboradorId);
        if (conexionesIds.isEmpty()) {
            return;
        }

        try {
            String mensaje = serializar(tipoEvento, payload);
            for (String conexionId : conexionesIds) {
                openConnections.findByConnectionId(conexionId)
                        .ifPresent(conexion -> conexion.sendTextAndAwait(mensaje));
            }
        } catch (RuntimeException e) {
            LOG.errorf(e, "Fallo al notificar por websocket al colaborador %d", colaboradorId);
            throw e;
        }
    }

    private String serializar(String tipoEvento, Object payload) {
        // Map.of() no admite valores null, y tipoEvento (eventoOrigen) es null
        // para notificaciones creadas manualmente via REST - por eso un mapa mutable.
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("tipoEvento", tipoEvento);
        cuerpo.put("payload", payload);
        try {
            return objectMapper.writeValueAsString(cuerpo);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el mensaje de notificacion en tiempo real", e);
        }
    }
}
