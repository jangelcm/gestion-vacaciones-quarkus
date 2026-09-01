package com.vacaciones.notificaciones.infraestructura.adaptadores.out.websocket;

import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SesionesWebSocketRegistry {

    private final Map<Long, Set<WebSocketConnection>> conexionesPorColaborador = new ConcurrentHashMap<>();

    public void registrar(Long colaboradorId, WebSocketConnection conexion) {
        conexionesPorColaborador
                .computeIfAbsent(colaboradorId, id -> ConcurrentHashMap.newKeySet())
                .add(conexion);
    }

    public void remover(Long colaboradorId, WebSocketConnection conexion) {
        Set<WebSocketConnection> conexiones = conexionesPorColaborador.get(colaboradorId);
        if (conexiones == null) {
            return;
        }
        conexiones.remove(conexion);
        if (conexiones.isEmpty()) {
            conexionesPorColaborador.remove(colaboradorId);
        }
    }

    public Set<WebSocketConnection> obtenerConexiones(Long colaboradorId) {
        return conexionesPorColaborador.getOrDefault(colaboradorId, Set.of());
    }
}
