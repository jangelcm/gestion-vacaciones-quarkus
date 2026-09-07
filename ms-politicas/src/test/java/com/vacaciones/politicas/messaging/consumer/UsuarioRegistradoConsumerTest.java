package com.vacaciones.politicas.messaging.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.messaging.event.UsuarioRegistradoEvent;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UsuarioRegistradoConsumerTest {

    private static final Long COLABORADOR_NUEVO = 93001L;
    private static final Long COLABORADOR_IDEMPOTENCIA = 93002L;

    @Inject
    @Connector("smallrye-in-memory")
    InMemoryConnector connector;

    @Inject
    SaldoDiasRepository saldoDiasRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        connector.clear();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldAssignDefaultPoliticaWhenUsuarioRegistradoEventArrives() throws Exception {
        publishEvent(new UsuarioRegistradoEvent(COLABORADOR_NUEVO, "nuevo.usuario", "nuevo@empresa.com"));

        awaitUntil(() -> saldoDiasRepository.findByColaboradorId(COLABORADOR_NUEVO) != null);

        SaldoDiasEntity saldo = saldoDiasRepository.findByColaboradorId(COLABORADOR_NUEVO);
        assertEquals(new BigDecimal("30.0"), saldo.getDiasDisponibles());
        assertTrue(saldo.getPolitica().getEsPorDefecto());
    }

    @Test
    void shouldBeIdempotentWhenUsuarioRegistradoEventArrivesTwice() throws Exception {
        UsuarioRegistradoEvent evento =
                new UsuarioRegistradoEvent(COLABORADOR_IDEMPOTENCIA, "duplicado.usuario", "duplicado@empresa.com");

        publishEvent(evento);
        awaitUntil(() -> saldoDiasRepository.findByColaboradorId(COLABORADOR_IDEMPOTENCIA) != null);
        publishEvent(evento);
        Thread.sleep(500);

        assertEquals(1, saldoDiasRepository.count("colaboradorId", COLABORADOR_IDEMPOTENCIA));
        assertEquals(
                new BigDecimal("30.0"),
                saldoDiasRepository.findByColaboradorId(COLABORADOR_IDEMPOTENCIA).getDiasDisponibles());
    }

    private void publishEvent(UsuarioRegistradoEvent evento) {
        try {
            InMemorySource<String> source = connector.source("usuario-registrado-in");
            source.send(objectMapper.writeValueAsString(evento));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void awaitUntil(java.util.concurrent.Callable<Boolean> condition) throws Exception {
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline) {
            if (condition.call()) {
                return;
            }
            Thread.sleep(100);
        }
        fail("Timeout esperando procesamiento del evento");
    }
}
