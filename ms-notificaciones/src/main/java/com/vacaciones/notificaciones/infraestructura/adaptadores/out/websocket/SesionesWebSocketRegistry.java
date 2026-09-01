package com.vacaciones.notificaciones.infraestructura.adaptadores.out.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guarda solo el id de cada conexion (String), nunca el objeto WebSocketConnection
 * en si: es un proxy CDI atado al contexto de esa conexion, e invocarlo mas tarde
 * desde otro hilo/contexto lanza ContextNotActiveException. Para enviar un mensaje
 * despues, se resuelve la conexion real por id via OpenConnections en ese momento.
 */
@ApplicationScoped
public class SesionesWebSocketRegistry {

    private final Map<Long, Set<String>> conexionesPorColaborador = new ConcurrentHashMap<>();

    public void registrar(Long colaboradorId, String conexionId) {
        conexionesPorColaborador
                .computeIfAbsent(colaboradorId, id -> ConcurrentHashMap.newKeySet())
                .add(conexionId);
    }

    public void remover(Long colaboradorId, String conexionId) {
        Set<String> conexiones = conexionesPorColaborador.get(colaboradorId);
        if (conexiones == null) {
            return;
        }
        conexiones.remove(conexionId);
        if (conexiones.isEmpty()) {
            conexionesPorColaborador.remove(colaboradorId);
        }
    }

    public Set<String> obtenerConexiones(Long colaboradorId) {
        return conexionesPorColaborador.getOrDefault(colaboradorId, Set.of());
    }
}
