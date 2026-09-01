package com.vacaciones.notificaciones.infraestructura.adaptadores.out.websocket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.websockets.next.OpenConnections;
import io.quarkus.websockets.next.WebSocketConnection;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WebSocketNotificadorAdapterTest {

    private final SesionesWebSocketRegistry registro = new SesionesWebSocketRegistry();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OpenConnections openConnections = mock(OpenConnections.class);
    private final WebSocketNotificadorAdapter adapter =
            new WebSocketNotificadorAdapter(registro, objectMapper, openConnections);

    @Test
    void shouldSendMessageToConnectionWhenColaboradorHasActiveSession() {
        WebSocketConnection conexion = mock(WebSocketConnection.class);
        when(conexion.id()).thenReturn("conexion-1");
        registro.registrar(1001L, "conexion-1");
        when(openConnections.findByConnectionId("conexion-1")).thenReturn(Optional.of(conexion));

        adapter.notificar(1001L, "solicitud.aprobada", Map.of("dias", 5));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(conexion).sendTextAndAwait(captor.capture());
        assertTrue(captor.getValue().contains("solicitud.aprobada"));
    }

    @Test
    void shouldNotThrowWhenColaboradorHasNoActiveSession() {
        assertDoesNotThrow(() -> adapter.notificar(9999L, "solicitud.aprobada", Map.of("dias", 5)));
    }

    @Test
    void shouldNotThrowWhenTipoEventoIsNull() {
        WebSocketConnection conexion = mock(WebSocketConnection.class);
        when(conexion.id()).thenReturn("conexion-2");
        registro.registrar(1001L, "conexion-2");
        when(openConnections.findByConnectionId("conexion-2")).thenReturn(Optional.of(conexion));

        assertDoesNotThrow(() -> adapter.notificar(1001L, null, Map.of("dias", 5)));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(conexion).sendTextAndAwait(captor.capture());
        assertTrue(captor.getValue().contains("\"tipoEvento\":null"));
    }

    @Test
    void shouldNotThrowWhenRegisteredConnectionIsNoLongerOpen() {
        registro.registrar(1001L, "conexion-cerrada");
        when(openConnections.findByConnectionId("conexion-cerrada")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> adapter.notificar(1001L, "solicitud.aprobada", Map.of("dias", 5)));
    }
}
