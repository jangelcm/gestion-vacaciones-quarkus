package com.vacaciones.notificaciones.infraestructura.adaptadores.out.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacaciones.notificaciones.dominio.port.out.NotificadorTiempoRealPort;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class WebSocketNotificadorAdapter implements NotificadorTiempoRealPort {

    private final SesionesWebSocketRegistry registro;
    private final ObjectMapper objectMapper;

    public WebSocketNotificadorAdapter(SesionesWebSocketRegistry registro, ObjectMapper objectMapper) {
        this.registro = registro;
        this.objectMapper = objectMapper;
    }

    @Override
    public void notificar(Long colaboradorId, String tipoEvento, Object payload) {
        Set<WebSocketConnection> conexiones = registro.obtenerConexiones(colaboradorId);
        if (conexiones.isEmpty()) {
            return;
        }

        String mensaje = serializar(tipoEvento, payload);
        for (WebSocketConnection conexion : conexiones) {
            conexion.sendTextAndAwait(mensaje);
        }
    }

    private String serializar(String tipoEvento, Object payload) {
        try {
            return objectMapper.writeValueAsString(Map.of("tipoEvento", tipoEvento, "payload", payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el mensaje de notificacion en tiempo real", e);
        }
    }
}
