package com.vacaciones.notificaciones.infraestructura.adaptadores.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.model.TipoNotificacion;
import com.vacaciones.notificaciones.dominio.port.out.NotificacionRepositoryPort;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MongoNotificacionRepositoryAdapterTest {

    private static final Destinatario DESTINATARIO_1001 =
            new Destinatario(1001L, "colaborador1001@empresa.com", "Ana Perez");
    private static final Destinatario DESTINATARIO_2002 =
            new Destinatario(2002L, "colaborador2002@empresa.com", "Luis Gomez");

    @Inject
    NotificacionRepositoryPort repository;

    @Inject
    NotificacionPanacheRepository panacheRepository;

    @BeforeEach
    void limpiarColeccion() {
        panacheRepository.deleteAll();
    }

    @Test
    void shouldPersistAndReturnNotificacionWithIdAssigned() {
        Notificacion notificacion = new Notificacion(
                "evt-guardar-1", TipoNotificacion.EMAIL, DESTINATARIO_1001, "asunto", "cuerpo", "solicitud.aprobada");

        Notificacion guardada = repository.guardar(notificacion);

        assertNotNull(guardada.getId());
        assertEquals("evt-guardar-1", guardada.getEventoId());
        assertEquals(DESTINATARIO_1001, guardada.getDestinatario());
        assertEquals("asunto", guardada.getAsunto());
    }

    @Test
    void shouldReturnTrueWhenEventoIdExists() {
        repository.guardar(new Notificacion(
                "evt-existe-1", TipoNotificacion.EMAIL, DESTINATARIO_1001, "asunto", "cuerpo", "solicitud.aprobada"));

        assertTrue(repository.existePorEventoId("evt-existe-1"));
    }

    @Test
    void shouldReturnFalseWhenEventoIdDoesNotExist() {
        assertFalse(repository.existePorEventoId("evt-no-existe"));
    }

    @Test
    void shouldReturnNotificacionesByColaborador() {
        repository.guardar(new Notificacion(
                "evt-col-1", TipoNotificacion.EMAIL, DESTINATARIO_1001, "asunto1", "cuerpo1", "solicitud.aprobada"));
        repository.guardar(new Notificacion(
                "evt-col-2", TipoNotificacion.WEBSOCKET, DESTINATARIO_1001, "asunto2", "cuerpo2", "solicitud.cancelada"));
        repository.guardar(new Notificacion(
                "evt-col-3", TipoNotificacion.EMAIL, DESTINATARIO_2002, "asunto3", "cuerpo3", "solicitud.aprobada"));

        List<Notificacion> resultado = repository.buscarPorColaborador(1001L);

        assertEquals(2, resultado.size());
    }

    @Test
    void shouldReturnEmptyListWhenColaboradorHasNoNotificaciones() {
        List<Notificacion> resultado = repository.buscarPorColaborador(9999L);

        assertTrue(resultado.isEmpty());
    }
}
