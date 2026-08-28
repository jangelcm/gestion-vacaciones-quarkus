package com.vacaciones.politicas.messaging.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vacaciones.politicas.entity.MovimientoSaldoEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.exception.ResourceNotFoundException;
import com.vacaciones.politicas.messaging.event.SolicitudAprobadaEvent;
import com.vacaciones.politicas.repository.MovimientoSaldoRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import com.vacaciones.politicas.testsupport.SaldoTestDataHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SolicitudAprobadaConsumerTest {

    private static final Long COLABORADOR_CONSUMO = 91001L;
    private static final Long COLABORADOR_IDEMPOTENCIA = 91002L;
    private static final Long COLABORADOR_SIN_SALDO = 9999L;
    private static final Long COLABORADOR_CONCURRENCIA = 91004L;

    private static final Long SOLICITUD_ID = 9001L;
    private static final String EVENTO_CONSUMO_ID = "evt-aprobada-91001";
    private static final String EVENTO_IDEMPOTENCIA_ID = "evt-aprobada-91002";

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
    SolicitudAprobadaConsumer consumer;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        connector.clear();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void shouldConsumeSolicitudAprobadaAndUpdateSaldoAndCreateMovimiento() throws Exception {
        saldoTestDataHelper.seedSaldo(COLABORADOR_CONSUMO, "10.0", "2.0");

        publishEvent(buildEvent(EVENTO_CONSUMO_ID, SOLICITUD_ID, COLABORADOR_CONSUMO, new BigDecimal("3.0")));

        awaitUntil(() -> movimientoSaldoRepository.existsByEventoId(EVENTO_CONSUMO_ID));

        SaldoDiasEntity saldo = saldoDiasRepository.findByColaboradorId(COLABORADOR_CONSUMO);
        assertEquals(new BigDecimal("7.0"), saldo.getDiasDisponibles());
        assertEquals(new BigDecimal("5.0"), saldo.getDiasUsados());

        List<MovimientoSaldoEntity> movimientos = movimientoSaldoRepository
                .find("eventoId", EVENTO_CONSUMO_ID).list();

        assertEquals(1, movimientos.size());
        MovimientoSaldoEntity movimiento = movimientos.get(0);
        assertEquals(SOLICITUD_ID, movimiento.getSolicitudId());
        assertEquals("DESCUENTO", movimiento.getTipoMovimiento());
        assertEquals(new BigDecimal("3.0"), movimiento.getDias());
        assertEquals("solicitud.aprobada", movimiento.getEventoOrigen());
        assertEquals(EVENTO_CONSUMO_ID, movimiento.getEventoId());
        assertEquals(saldo.getId(), movimiento.getSaldo().getId());
    }

    @Test
    void shouldBeIdempotentWhenSameEventoIdIsPublishedTwice() throws Exception {
        saldoTestDataHelper.seedSaldo(COLABORADOR_IDEMPOTENCIA, "10.0", "2.0");

        SolicitudAprobadaEvent evento = buildEvent(
                EVENTO_IDEMPOTENCIA_ID, SOLICITUD_ID, COLABORADOR_IDEMPOTENCIA, new BigDecimal("3.0"));

        publishEvent(evento);
        publishEvent(evento);

        awaitUntil(() -> movimientoSaldoRepository.existsByEventoId(EVENTO_IDEMPOTENCIA_ID));

        SaldoDiasEntity saldo = saldoDiasRepository.findByColaboradorId(COLABORADOR_IDEMPOTENCIA);
        assertEquals(new BigDecimal("7.0"), saldo.getDiasDisponibles());
        assertEquals(new BigDecimal("5.0"), saldo.getDiasUsados());
        assertEquals(1, movimientoSaldoRepository.count("eventoId", EVENTO_IDEMPOTENCIA_ID));
    }

    @Test
    void shouldThrowWhenColaboradorHasNoSaldoAndNotCreateMovimiento() throws Exception {
        long movimientosAntes = movimientoSaldoRepository.count();

        SolicitudAprobadaEvent evento = buildEvent(
                "evt-sin-saldo", 9002L, COLABORADOR_SIN_SALDO, new BigDecimal("2.0"));

        assertThrows(
                ResourceNotFoundException.class,
                () -> consumer.onSolicitudAprobada(toJson(evento)));

        assertEquals(movimientosAntes, movimientoSaldoRepository.count());
        assertFalse(movimientoSaldoRepository.existsByEventoId("evt-sin-saldo"));
        assertEquals(null, saldoDiasRepository.findByColaboradorId(COLABORADOR_SIN_SALDO));
    }

    @Test
    void shouldApplyBothDiscountsWhenTwoEventsArriveConcurrently() throws Exception {
        saldoTestDataHelper.seedSaldo(COLABORADOR_CONCURRENCIA, "10.0", "0.0");

        SolicitudAprobadaEvent evento1 = buildEvent(
                "evt-conc-91004-1", 9001L, COLABORADOR_CONCURRENCIA, new BigDecimal("2.0"));
        SolicitudAprobadaEvent evento2 = buildEvent(
                "evt-conc-91004-2", 9002L, COLABORADOR_CONCURRENCIA, new BigDecimal("3.0"));

        CompletableFuture<Void> hilo1 = CompletableFuture.runAsync(() -> publishEvent(evento1));
        CompletableFuture<Void> hilo2 = CompletableFuture.runAsync(() -> publishEvent(evento2));
        CompletableFuture.allOf(hilo1, hilo2).join();

        awaitUntil(() -> movimientoSaldoRepository.existsByEventoId("evt-conc-91004-1")
                && movimientoSaldoRepository.existsByEventoId("evt-conc-91004-2"));

        SaldoDiasEntity saldo = saldoDiasRepository.findByColaboradorId(COLABORADOR_CONCURRENCIA);
        assertEquals(new BigDecimal("5.0"), saldo.getDiasDisponibles());
        assertEquals(new BigDecimal("5.0"), saldo.getDiasUsados());
        assertTrue(movimientoSaldoRepository.existsByEventoId("evt-conc-91004-1"));
        assertTrue(movimientoSaldoRepository.existsByEventoId("evt-conc-91004-2"));
        assertEquals(2, movimientoSaldoRepository.count("eventoId in ?1",
                List.of("evt-conc-91004-1", "evt-conc-91004-2")));
    }

    private SolicitudAprobadaEvent buildEvent(
            String eventoId,
            Long solicitudId,
            Long colaboradorId,
            BigDecimal diasAprobados) {
        return new SolicitudAprobadaEvent(
                eventoId,
                solicitudId,
                colaboradorId,
                diasAprobados,
                LocalDateTime.of(2026, 8, 27, 16, 0, 0));
    }

    private void publishEvent(SolicitudAprobadaEvent evento) {
        try {
            InMemorySource<String> source = connector.source("solicitud-aprobada-in");
            source.send(toJson(evento));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String toJson(SolicitudAprobadaEvent evento) throws JsonProcessingException {
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
