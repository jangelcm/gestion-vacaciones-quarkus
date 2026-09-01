package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.model.EstadoNotificacion;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.model.TipoNotificacion;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudRechazadaEvent;
import com.vacaciones.notificaciones.testsupport.EnviarNotificacionUseCaseMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.inject.Inject;
import java.time.LocalDate;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@QuarkusTest
class SolicitudRechazadaConsumerTest {

    @Inject
    @Connector("smallrye-in-memory")
    InMemoryConnector connector;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        connector.clear();
        Mockito.reset(EnviarNotificacionUseCaseMock.DELEGATE);
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void shouldMapEventAndCallUseCaseWhenSolicitudRechazadaArrives() throws Exception {
        SolicitudRechazadaEvent evento = new SolicitudRechazadaEvent(
                "evt-1", 9001L, 1001L, "colaborador@empresa.com", "Ana Perez",
                "Periodo de alta demanda operativa",
                LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 15));

        InMemorySource<String> source = connector.source("solicitud-rechazada-in");
        source.send(objectMapper.writeValueAsString(evento));

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        Mockito.verify(EnviarNotificacionUseCaseMock.DELEGATE, Mockito.timeout(5000)).enviar(captor.capture());

        Notificacion notificacion = captor.getValue();
        assertEquals("evt-1", notificacion.getEventoId());
        assertEquals(TipoNotificacion.EMAIL, notificacion.getTipo());
        assertEquals(
                new Destinatario(1001L, "colaborador@empresa.com", "Ana Perez"),
                notificacion.getDestinatario());
        assertEquals("solicitud.rechazada", notificacion.getEventoOrigen());
        assertEquals(EstadoNotificacion.PENDIENTE, notificacion.getEstado());
        assertEquals("Tu solicitud de vacaciones fue rechazada", notificacion.getAsunto());
        assertEquals(
                "Hola Ana Perez, tu solicitud de vacaciones del 2026-09-10 al 2026-09-15 fue rechazada. "
                        + "Motivo: Periodo de alta demanda operativa.",
                notificacion.getCuerpo());
    }
}
