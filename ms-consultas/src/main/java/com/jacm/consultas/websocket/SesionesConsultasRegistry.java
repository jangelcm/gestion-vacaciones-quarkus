package com.jacm.consultas.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SesionesConsultasRegistry {

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
