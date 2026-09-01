package com.vacaciones.notificaciones.infraestructura.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.model.EstadoNotificacion;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.model.TipoNotificacion;
import com.vacaciones.notificaciones.infraestructura.producer.event.NotificacionEnviadaEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class NotificacionEnviadaEventProducerTest {

    @Inject
    @Connector("smallrye-in-memory")
    InMemoryConnector connector;

    @Inject
    NotificacionEnviadaEventProducer producer;

    @BeforeEach
    void limpiar() {
        connector.sink("notificacion-enviada-out").clear();
    }

    @Test
    void shouldPublishNotificacionEnviadaEventWithNotificacionData() {
        Notificacion notificacion = new Notificacion(
                "id-mongo-1", "evt-1", TipoNotificacion.EMAIL,
                new Destinatario(1001L, "colaborador@empresa.com", "Ana Perez"),
                "asunto", "cuerpo", EstadoNotificacion.ENVIADO, "solicitud.aprobada",
                LocalDateTime.now(), LocalDateTime.now());

        producer.publicarResultado(notificacion);

        InMemorySink<NotificacionEnviadaEvent> sink = connector.sink("notificacion-enviada-out");
        assertEquals(1, sink.received().size());

        NotificacionEnviadaEvent evento = sink.received().get(0).getPayload();
        assertEquals("id-mongo-1", evento.notificacionId());
        assertEquals(1001L, evento.colaboradorId());
        assertEquals(TipoNotificacion.EMAIL, evento.tipo());
        assertEquals(EstadoNotificacion.ENVIADO, evento.estado());
        assertEquals("solicitud.aprobada", evento.eventoOrigen());
        assertNotNull(evento.fechaEvento());
    }
}
