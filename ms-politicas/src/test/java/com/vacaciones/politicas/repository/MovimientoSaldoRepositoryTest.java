package com.vacaciones.politicas.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vacaciones.politicas.entity.MovimientoSaldoEntity;
import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MovimientoSaldoRepositoryTest {

    @Inject
    PoliticaRepository politicaRepository;

    @Inject
    SaldoDiasRepository saldoDiasRepository;

    @Inject
    MovimientoSaldoRepository movimientoSaldoRepository;

    @Test
    @TestTransaction
    void shouldSaveMovimientoSaldoLinkedToExistingSaldo() {
        PoliticaEntity politica = PoliticaEntity.builder()
                .nombre("Vacaciones anuales")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(15)
                .antiguedadMinimaMeses(0)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(30)
                .activa(Boolean.TRUE)
                .build();

        politicaRepository.persist(politica);

        SaldoDiasEntity saldoDias = SaldoDiasEntity.builder()
                .colaboradorId(3003L)
                .politica(politica)
                .diasDisponibles(new BigDecimal("12.0"))
                .diasUsados(new BigDecimal("3.0"))
                .diasAcumulados(new BigDecimal("1.0"))
                .build();

        saldoDiasRepository.persist(saldoDias);

        MovimientoSaldoEntity movimiento = MovimientoSaldoEntity.builder()
                .saldo(saldoDias)
                .solicitudId(9001L)
                .tipoMovimiento("APROBACION")
                .dias(new BigDecimal("2.0"))
                .eventoOrigen("solicitud.aprobada")
                .eventoId("evt-9001")
                .build();

        movimientoSaldoRepository.persist(movimiento);

        assertNotNull(movimiento.getId());

        MovimientoSaldoEntity persisted = movimientoSaldoRepository.findById(movimiento.getId());

        assertNotNull(persisted);
        assertEquals(saldoDias.getId(), persisted.getSaldo().getId());
        assertEquals("evt-9001", persisted.getEventoId());
    }

    @Test
    @TestTransaction
    void shouldReturnTrueWhenEventoIdExistsAndFalseWhenItDoesNot() {
        PoliticaEntity politica = PoliticaEntity.builder()
                .nombre("Vacaciones premium")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(20)
                .antiguedadMinimaMeses(12)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(40)
                .activa(Boolean.TRUE)
                .build();

        politicaRepository.persist(politica);

        SaldoDiasEntity saldoDias = SaldoDiasEntity.builder()
                .colaboradorId(4004L)
                .politica(politica)
                .diasDisponibles(new BigDecimal("18.0"))
                .diasUsados(new BigDecimal("1.0"))
                .diasAcumulados(new BigDecimal("0.0"))
                .build();

        saldoDiasRepository.persist(saldoDias);

        MovimientoSaldoEntity movimiento = MovimientoSaldoEntity.builder()
                .saldo(saldoDias)
                .solicitudId(9002L)
                .tipoMovimiento("CANCELACION")
                .dias(new BigDecimal("1.0"))
                .eventoOrigen("solicitud.cancelada")
                .eventoId("evt-idempotencia-1")
                .build();

        movimientoSaldoRepository.persist(movimiento);

        assertTrue(movimientoSaldoRepository.existsByEventoId("evt-idempotencia-1"));
        assertFalse(movimientoSaldoRepository.existsByEventoId("evt-idempotencia-no-existe"));
    }
}