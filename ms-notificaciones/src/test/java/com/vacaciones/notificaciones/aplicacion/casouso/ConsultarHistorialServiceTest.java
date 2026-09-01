package com.vacaciones.notificaciones.aplicacion.casouso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.model.TipoNotificacion;
import com.vacaciones.notificaciones.dominio.port.out.NotificacionRepositoryPort;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultarHistorialServiceTest {

    private static final Destinatario DESTINATARIO =
            new Destinatario(1001L, "colaborador@empresa.com", "Ana Perez");

    @Mock
    NotificacionRepositoryPort repository;

    @InjectMocks
    ConsultarHistorialService service;

    @Test
    void shouldDelegateToRepositoryAndReturnItsResult() {
        Notificacion notificacion = new Notificacion(
                "evt-1", TipoNotificacion.EMAIL, DESTINATARIO, "asunto", "cuerpo", "solicitud.aprobada");
        when(repository.buscarPorColaborador(1001L)).thenReturn(List.of(notificacion));

        List<Notificacion> resultado = service.consultarPorColaborador(1001L);

        assertEquals(1, resultado.size());
        assertEquals(notificacion, resultado.get(0));
        verify(repository).buscarPorColaborador(1001L);
    }

    @Test
    void shouldReturnEmptyListWhenRepositoryHasNoNotifications() {
        when(repository.buscarPorColaborador(9999L)).thenReturn(List.of());

        List<Notificacion> resultado = service.consultarPorColaborador(9999L);

        assertTrue(resultado.isEmpty());
        verify(repository).buscarPorColaborador(9999L);
    }
}
