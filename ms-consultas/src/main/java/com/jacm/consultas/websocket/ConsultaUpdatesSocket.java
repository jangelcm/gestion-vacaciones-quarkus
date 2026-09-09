package com.jacm.consultas.websocket;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.PathParam;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;

@WebSocket(path = "/consultas-updates/{colaboradorId}")
public class ConsultaUpdatesSocket {

    private final SesionesConsultasRegistry registro;
    private final WebSocketConnection conexion;

    public ConsultaUpdatesSocket(SesionesConsultasRegistry registro, WebSocketConnection conexion) {
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
