package com.vacaciones.notificaciones.infraestructura.adaptadores.out.websocket;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.PathParam;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;

@WebSocket(path = "/notificaciones/{colaboradorId}")
public class NotificacionSocket {

    private final SesionesWebSocketRegistry registro;
    private final WebSocketConnection conexion;

    public NotificacionSocket(SesionesWebSocketRegistry registro, WebSocketConnection conexion) {
        this.registro = registro;
        this.conexion = conexion;
    }

    @OnOpen
    public void onOpen(@PathParam("colaboradorId") String colaboradorId) {
        registro.registrar(Long.valueOf(colaboradorId), conexion.id());
    }

    @OnClose
    public void onClose(@PathParam("colaboradorId") String colaboradorId) {
        registro.remover(Long.valueOf(colaboradorId), conexion.id());
    }
}
