package com.vacaciones.politicas.messaging.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vacaciones.politicas.entity.MovimientoSaldoEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.messaging.event.SolicitudCanceladaEvent;
import com.vacaciones.politicas.repository.MovimientoSaldoRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import com.vacaciones.politicas.testsupport.SaldoTestDataHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SolicitudCanceladaConsumerTest {

    private static final Long COLABORADOR_DEVOLUCION = 92001L;
    private static final Long COLABORADOR_IDEMPOTENCIA = 92002L;
    private static final Long COLABORADOR_BORDE = 92003L;

    private static final Long SOLICITUD_ID = 9101L;
    private static final String EVENTO_DEVOLUCION_ID = "evt-cancelada-92001";
    private static final String EVENTO_IDEMPOTENCIA_ID = "evt-cancelada-92002";
    private static final String EVENTO_BORDE_ID = "evt-cancelada-92003";

    @Inject
    @Connector("smallrye-in-memory")
    InMemoryConnector connector;

    @Inject
    SaldoTestDataHelper saldoTestDataHelper;

    @Inject
    SaldoDiasRepository saldoDiasRepository;

    @Inject
    MovimientoSaldoRepository movimientoSaldoRepository;

    @Inject
    SolicitudCanceladaConsumer consumer;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        connector.clear();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void shouldConsumeSolicitudCanceladaAndUpdateSaldoAndCreateMovimiento() throws Exception {
        saldoTestDataHelper.seedSaldo(COLABORADOR_DEVOLUCION, "7.0", "5.0");

        publishEvent(buildEvent(EVENTO_DEVOLUCION_ID, SOLICITUD_ID, COLABORADOR_DEVOLUCION, new BigDecimal("3.0")));

        awaitUntil(() -> movimientoSaldoRepository.existsByEventoId(EVENTO_DEVOLUCION_ID));

        SaldoDiasEntity saldo = saldoDiasRepository.findByColaboradorId(COLABORADOR_DEVOLUCION);
        assertEquals(new BigDecimal("10.0"), saldo.getDiasDisponibles());
        assertEquals(new BigDecimal("2.0"), saldo.getDiasUsados());

        MovimientoSaldoEntity movimiento = movimientoSaldoRepository
                .find("eventoId", EVENTO_DEVOLUCION_ID).firstResult();
        assertEquals(SOLICITUD_ID, movimiento.getSolicitudId());
        assertEquals("DEVOLUCION", movimiento.getTipoMovimiento());
        assertEquals(new BigDecimal("3.0"), movimiento.getDias());
        assertEquals("solicitud.cancelada", movimiento.getEventoOrigen());
        assertEquals(EVENTO_DEVOLUCION_ID, movimiento.getEventoId());
        assertEquals(saldo.getId(), movimiento.getSaldo().getId());
    }

    @Test
    void shouldBeIdempotentWhenSameEventoIdIsPublishedTwice() throws Exception {
        saldoTestDataHelper.seedSaldo(COLABORADOR_IDEMPOTENCIA, "7.0", "5.0");

        SolicitudCanceladaEvent evento = buildEvent(
                EVENTO_IDEMPOTENCIA_ID, SOLICITUD_ID, COLABORADOR_IDEMPOTENCIA, new BigDecimal("3.0"));

        publishEvent(evento);
        publishEvent(evento);

        awaitUntil(() -> movimientoSaldoRepository.existsByEventoId(EVENTO_IDEMPOTENCIA_ID));

        SaldoDiasEntity saldo = saldoDiasRepository.findByColaboradorId(COLABORADOR_IDEMPOTENCIA);
        assertEquals(new BigDecimal("10.0"), saldo.getDiasDisponibles());
        assertEquals(new BigDecimal("2.0"), saldo.getDiasUsados());
        assertEquals(1, movimientoSaldoRepository.count("eventoId", EVENTO_IDEMPOTENCIA_ID));
    }

    @Test
    void shouldFloorDiasUsadosAtZeroWhenDevolucionExceedsUsados() throws Exception {
        saldoTestDataHelper.seedSaldo(COLABORADOR_BORDE, "5.0", "2.0");

        publishEvent(buildEvent(EVENTO_BORDE_ID, SOLICITUD_ID, COLABORADOR_BORDE, new BigDecimal("5.0")));

        awaitUntil(() -> movimientoSaldoRepository.existsByEventoId(EVENTO_BORDE_ID));

        SaldoDiasEntity saldo = saldoDiasRepository.findByColaboradorId(COLABORADOR_BORDE);
        assertEquals(new BigDecimal("10.0"), saldo.getDiasDisponibles());
        assertEquals(BigDecimal.ZERO.setScale(1), saldo.getDiasUsados());

        MovimientoSaldoEntity movimiento = movimientoSaldoRepository
                .find("eventoId", EVENTO_BORDE_ID).firstResult();
        assertEquals(new BigDecimal("5.0"), movimiento.getDias());
    }

    private SolicitudCanceladaEvent buildEvent(
            String eventoId,
            Long solicitudId,
            Long colaboradorId,
            BigDecimal diasADevolver) {
        return new SolicitudCanceladaEvent(
                eventoId,
                solicitudId,
                colaboradorId,
                diasADevolver,
                LocalDateTime.of(2026, 8, 28, 16, 0, 0));
    }

    private void publishEvent(SolicitudCanceladaEvent evento) {
        try {
            InMemorySource<String> source = connector.source("solicitud-cancelada-in");
            source.send(toJson(evento));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String toJson(SolicitudCanceladaEvent evento) throws JsonProcessingException {
        return objectMapper.writeValueAsString(evento);
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
