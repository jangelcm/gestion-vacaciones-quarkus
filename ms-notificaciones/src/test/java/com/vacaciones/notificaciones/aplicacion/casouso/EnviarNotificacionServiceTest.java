package com.vacaciones.notificaciones.aplicacion.casouso;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.model.EstadoNotificacion;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.model.TipoNotificacion;
import com.vacaciones.notificaciones.dominio.port.out.EnviadorEmailPort;
import com.vacaciones.notificaciones.dominio.port.out.NotificacionRepositoryPort;
import com.vacaciones.notificaciones.dominio.port.out.NotificadorTiempoRealPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnviarNotificacionServiceTest {

    private static final Destinatario DESTINATARIO =
            new Destinatario(1001L, "colaborador@empresa.com", "Ana Perez");

    @Mock
    NotificacionRepositoryPort repository;

    @Mock
    EnviadorEmailPort enviadorEmailPort;

    @Mock
    NotificadorTiempoRealPort notificadorTiempoRealPort;

    @InjectMocks
    EnviarNotificacionService service;

    @Test
    void shouldSendEmailAndMarkAsEnviadaWhenTipoIsEmail() {
        Notificacion notificacion = new Notificacion(
                "evt-1", TipoNotificacion.EMAIL, DESTINATARIO, "asunto", "cuerpo", "solicitud.aprobada");
        when(repository.existePorEventoId("evt-1")).thenReturn(false);

        service.enviar(notificacion);

        verify(enviadorEmailPort).enviar(DESTINATARIO, "asunto", "cuerpo");
        verify(notificadorTiempoRealPort, never()).notificar(any(), any(), any());
        assertEquals(EstadoNotificacion.ENVIADO, notificacion.getEstado());
        verify(repository).guardar(notificacion);
    }

    @Test
    void shouldNotifyWebsocketAndMarkAsEnviadaWhenTipoIsWebsocket() {
        Notificacion notificacion = new Notificacion(
                "evt-2", TipoNotificacion.WEBSOCKET, DESTINATARIO, "asunto", "cuerpo", "solicitud.aprobada");
        when(repository.existePorEventoId("evt-2")).thenReturn(false);

        service.enviar(notificacion);

        verify(notificadorTiempoRealPort).notificar(1001L, "solicitud.aprobada", notificacion);
        verify(enviadorEmailPort, never()).enviar(any(), any(), any());
        assertEquals(EstadoNotificacion.ENVIADO, notificacion.getEstado());
        verify(repository).guardar(notificacion);
    }

    @Test
    void shouldCallBothPortsWhenTipoIsRecordatorio() {
        Notificacion notificacion = new Notificacion(
                "evt-3", TipoNotificacion.RECORDATORIO, DESTINATARIO, "asunto", "cuerpo", "solicitud.aprobada");
        when(repository.existePorEventoId("evt-3")).thenReturn(false);

        service.enviar(notificacion);

        verify(enviadorEmailPort).enviar(DESTINATARIO, "asunto", "cuerpo");
        verify(notificadorTiempoRealPort).notificar(1001L, "solicitud.aprobada", notificacion);
        assertEquals(EstadoNotificacion.ENVIADO, notificacion.getEstado());
        verify(repository).guardar(notificacion);
    }

    @Test
    void shouldDoNothingWhenEventoIdAlreadyExists() {
        Notificacion notificacion = new Notificacion(
                "evt-duplicado", TipoNotificacion.EMAIL, DESTINATARIO, "asunto", "cuerpo", "solicitud.aprobada");
        when(repository.existePorEventoId("evt-duplicado")).thenReturn(true);

        service.enviar(notificacion);

        verify(enviadorEmailPort, never()).enviar(any(), any(), any());
        verify(notificadorTiempoRealPort, never()).notificar(any(), any(), any());
        verify(repository, never()).guardar(any());
        assertEquals(EstadoNotificacion.PENDIENTE, notificacion.getEstado());
    }

    @Test
    void shouldSkipIdempotencyCheckWhenEventoIdIsNull() {
        Notificacion notificacion = new Notificacion(
                null, TipoNotificacion.EMAIL, DESTINATARIO, "asunto", "cuerpo", null);

        service.enviar(notificacion);

        verify(repository, never()).existePorEventoId(any());
        verify(enviadorEmailPort).enviar(DESTINATARIO, "asunto", "cuerpo");
        assertEquals(EstadoNotificacion.ENVIADO, notificacion.getEstado());
        verify(repository).guardar(notificacion);
    }

    @Test
    void shouldMarkAsFallidaAndNotRethrowWhenSendFails() {
        Notificacion notificacion = new Notificacion(
                "evt-4", TipoNotificacion.EMAIL, DESTINATARIO, "asunto", "cuerpo", "solicitud.aprobada");
        when(repository.existePorEventoId("evt-4")).thenReturn(false);
        doThrow(new RuntimeException("fallo SMTP simulado"))
                .when(enviadorEmailPort).enviar(any(), any(), any());

        assertDoesNotThrow(() -> service.enviar(notificacion));

        assertEquals(EstadoNotificacion.FALLIDO, notificacion.getEstado());
        verify(repository).guardar(notificacion);
    }
}
