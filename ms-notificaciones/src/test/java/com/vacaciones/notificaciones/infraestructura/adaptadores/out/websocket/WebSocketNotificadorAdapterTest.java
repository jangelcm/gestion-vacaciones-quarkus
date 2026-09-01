package com.vacaciones.notificaciones.infraestructura.adaptadores.out.websocket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.websockets.next.WebSocketConnection;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WebSocketNotificadorAdapterTest {

    private final SesionesWebSocketRegistry registro = new SesionesWebSocketRegistry();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebSocketNotificadorAdapter adapter =
            new WebSocketNotificadorAdapter(registro, objectMapper);

    @Test
    void shouldSendMessageToConnectionWhenColaboradorHasActiveSession() {
        WebSocketConnection conexion = mock(WebSocketConnection.class);
        registro.registrar(1001L, conexion);

        adapter.notificar(1001L, "solicitud.aprobada", Map.of("dias", 5));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(conexion).sendTextAndAwait(captor.capture());
        assertTrue(captor.getValue().contains("solicitud.aprobada"));
    }

    @Test
    void shouldNotThrowWhenColaboradorHasNoActiveSession() {
        assertDoesNotThrow(() -> adapter.notificar(9999L, "solicitud.aprobada", Map.of("dias", 5)));
    }
}
