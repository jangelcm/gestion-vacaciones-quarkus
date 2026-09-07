package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.model.EstadoNotificacion;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.model.TipoNotificacion;
import com.vacaciones.notificaciones.dominio.model.UsuarioInfo;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudCreadaEvent;
import com.vacaciones.notificaciones.testsupport.EnviarNotificacionUseCaseMock;
import com.vacaciones.notificaciones.testsupport.ResolverUsuarioPortMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@QuarkusTest
class SolicitudCreadaConsumerTest {

    @Inject
    @Connector("smallrye-in-memory")
    InMemoryConnector connector;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        connector.clear();
        Mockito.reset(EnviarNotificacionUseCaseMock.DELEGATE, ResolverUsuarioPortMock.DELEGATE);
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void shouldNotifyEveryAprobadorWhenSolicitudCreadaArrives() throws Exception {
        SolicitudCreadaEvent evento = new SolicitudCreadaEvent(
                9001L, "1001", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 15));
        Mockito.when(ResolverUsuarioPortMock.DELEGATE.resolverPorRol("Administrador"))
                .thenReturn(List.of(
                        new UsuarioInfo(6L, "luis.gomez@empresa.com", "Luis Gomez"),
                        new UsuarioInfo(7L, "maria.ruiz@empresa.com", "Maria Ruiz")));

        InMemorySource<String> source = connector.source("solicitud-creada-in");
        source.send(objectMapper.writeValueAsString(evento));

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        Mockito.verify(EnviarNotificacionUseCaseMock.DELEGATE, Mockito.timeout(5000).times(2))
                .enviar(captor.capture());

        List<Notificacion> notificaciones = captor.getAllValues();
        assertEquals(2, notificaciones.size());

        Notificacion paraLuis = notificaciones.stream()
                .filter(n -> n.getDestinatario().colaboradorId().equals(6L))
                .findFirst().orElseThrow();
        assertEquals("9001:6", paraLuis.getEventoId());
        assertEquals(TipoNotificacion.RECORDATORIO, paraLuis.getTipo());
        assertEquals(
                new Destinatario(6L, "luis.gomez@empresa.com", "Luis Gomez"),
                paraLuis.getDestinatario());
        assertEquals("solicitud.creada", paraLuis.getEventoOrigen());
        assertEquals(EstadoNotificacion.PENDIENTE, paraLuis.getEstado());
        assertEquals("Nueva solicitud de vacaciones pendiente de aprobación", paraLuis.getAsunto());
        assertEquals(
                "El colaborador con ID 1001 ha solicitado vacaciones del 2026-09-10 al 2026-09-15. "
                        + "Por favor revisa la solicitud.",
                paraLuis.getCuerpo());

        Notificacion paraMaria = notificaciones.stream()
                .filter(n -> n.getDestinatario().colaboradorId().equals(7L))
                .findFirst().orElseThrow();
        assertEquals("9001:7", paraMaria.getEventoId());
        assertEquals(
                new Destinatario(7L, "maria.ruiz@empresa.com", "Maria Ruiz"),
                paraMaria.getDestinatario());
    }
}
