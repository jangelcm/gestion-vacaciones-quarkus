package com.vacaciones.politicas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vacaciones.politicas.entity.MovimientoSaldoEntity;
import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.exception.ResourceNotFoundException;
import com.vacaciones.politicas.repository.MovimientoSaldoRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaldoDiasWriteOperationsTest {

    @Mock
    SaldoDiasRepository saldoDiasRepository;

    @Mock
    MovimientoSaldoRepository movimientoSaldoRepository;

    @Mock
    EntityManager entityManager;

    @InjectMocks
    SaldoDiasWriteOperations saldoDiasWriteOperations;

    @Test
    void shouldDescontarDiasAndCreateMovimientoWhenEventoIdIsNew() {
        SaldoDiasEntity saldoDias = buildSaldoDias("10.0", "2.0", "1.0");

        when(movimientoSaldoRepository.existsByEventoId("evt-aprobada-1")).thenReturn(false);
        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(saldoDias);
        when(saldoDiasRepository.getEntityManager()).thenReturn(entityManager);
        doNothing().when(movimientoSaldoRepository).persist(any(MovimientoSaldoEntity.class));

        SaldoDiasEntity resultado = saldoDiasWriteOperations.ejecutarDescuento(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.aprobada", "evt-aprobada-1");

        assertNotNull(resultado);
        assertEquals(new BigDecimal("7.0"), resultado.getDiasDisponibles());
        assertEquals(new BigDecimal("2.0"), resultado.getDiasUsados());
        assertEquals(new BigDecimal("3.0"), resultado.getDiasPendientes());

        ArgumentCaptor<MovimientoSaldoEntity> movimientoCaptor = ArgumentCaptor.forClass(MovimientoSaldoEntity.class);
        verify(movimientoSaldoRepository).persist(movimientoCaptor.capture());
        assertEquals("evt-aprobada-1", movimientoCaptor.getValue().getEventoId());
        assertEquals("DESCUENTO", movimientoCaptor.getValue().getTipoMovimiento());
    }

    @Test
    void shouldReturnNullWhenEventoIdAlreadyExists() {
        when(movimientoSaldoRepository.existsByEventoId("evt-duplicado-1")).thenReturn(true);

        SaldoDiasEntity resultado = saldoDiasWriteOperations.ejecutarDescuento(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.aprobada", "evt-duplicado-1");

        assertNull(resultado);
        verify(saldoDiasRepository, never()).findByColaboradorId(any());
        verify(movimientoSaldoRepository, never()).persist(any(MovimientoSaldoEntity.class));
    }

    @Test
    void shouldThrowWhenColaboradorHasNoSaldoOnDescuento() {
        when(movimientoSaldoRepository.existsByEventoId("evt-sin-saldo")).thenReturn(false);
        when(saldoDiasRepository.findByColaboradorId(9999L)).thenReturn(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> saldoDiasWriteOperations.ejecutarDescuento(
                        9999L, 9001L, new BigDecimal("2.0"), "solicitud.aprobada", "evt-sin-saldo"));

        verify(movimientoSaldoRepository, never()).persist(any(MovimientoSaldoEntity.class));
    }

    @Test
    void shouldDevolverDiasAndCreateMovimientoWhenEventoIdIsNew() {
        SaldoDiasEntity saldoDias = buildSaldoDias("7.0", "5.0", "1.0");

        when(movimientoSaldoRepository.existsByEventoId("evt-cancelada-1")).thenReturn(false);
        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(saldoDias);
        when(saldoDiasRepository.getEntityManager()).thenReturn(entityManager);
        doNothing().when(movimientoSaldoRepository).persist(any(MovimientoSaldoEntity.class));

        SaldoDiasEntity resultado = saldoDiasWriteOperations.ejecutarDevolucion(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.cancelada", "evt-cancelada-1");

        assertNotNull(resultado);
        assertEquals(new BigDecimal("10.0"), resultado.getDiasDisponibles());
        assertEquals(new BigDecimal("5.0"), resultado.getDiasUsados());
        assertEquals(new BigDecimal("0.0"), resultado.getDiasPendientes());

        ArgumentCaptor<MovimientoSaldoEntity> movimientoCaptor = ArgumentCaptor.forClass(MovimientoSaldoEntity.class);
        verify(movimientoSaldoRepository).persist(movimientoCaptor.capture());
        assertEquals("evt-cancelada-1", movimientoCaptor.getValue().getEventoId());
        assertEquals("DEVOLUCION", movimientoCaptor.getValue().getTipoMovimiento());
    }

    @Test
    void shouldIncrementDiasTrabajadosInProcesoDiario() {
        PoliticaEntity politica = PoliticaEntity.builder()
                .id(10L)
                .nombre("Vacaciones anuales")
                .diasBaseAnio(15)
                .maxDiasAcumulables(30)
                .acumulable(Boolean.TRUE)
                .activa(Boolean.TRUE)
                .build();
        SaldoDiasEntity saldo = SaldoDiasEntity.builder()
                .id(1L)
                .colaboradorId(1001L)
                .politica(politica)
                .diasDisponibles(new BigDecimal("6.0"))
                .diasUsados(new BigDecimal("9.0"))
                .diasAcumulados(new BigDecimal("2.0"))
                .diasPendientes(new BigDecimal("1.0"))
                .diasTrabajados(10)
                .version(0)
                .build();

        when(saldoDiasRepository.findById(1L)).thenReturn(saldo);
        when(saldoDiasRepository.getEntityManager()).thenReturn(entityManager);

        SaldoDiasEntity resultado = saldoDiasWriteOperations.ejecutarProcesoDiario(1L);

        assertEquals(11, resultado.getDiasTrabajados());
        assertEquals(new BigDecimal("2.0"), resultado.getDiasAcumulados());
        verify(saldoDiasRepository).persist(saldo);
    }

    @Test
    void shouldRolloverAtDay360AndCapAcumuladosAtMax() {
        PoliticaEntity politica = PoliticaEntity.builder()
                .id(10L)
                .nombre("Vacaciones anuales")
                .diasBaseAnio(15)
                .maxDiasAcumulables(10)
                .acumulable(Boolean.TRUE)
                .activa(Boolean.TRUE)
                .build();
        SaldoDiasEntity saldo = SaldoDiasEntity.builder()
                .id(1L)
                .colaboradorId(1001L)
                .politica(politica)
                .diasDisponibles(new BigDecimal("12.0"))
                .diasUsados(new BigDecimal("3.0"))
                .diasAcumulados(new BigDecimal("5.0"))
                                .diasPendientes(new BigDecimal("0.0"))
                                .diasTrabajados(359)
                .version(0)
                .build();

        when(saldoDiasRepository.findById(1L)).thenReturn(saldo);
        when(saldoDiasRepository.getEntityManager()).thenReturn(entityManager);

                SaldoDiasEntity resultado = saldoDiasWriteOperations.ejecutarProcesoDiario(1L);

        assertEquals(new BigDecimal("10.0"), resultado.getDiasAcumulados());
                assertEquals(0, resultado.getDiasTrabajados());
    }

    @Test
        void shouldRolloverAtDay360WithoutCapWhenNoMax() {
        PoliticaEntity politica = PoliticaEntity.builder()
                .id(10L)
                .nombre("Vacaciones anuales")
                .diasBaseAnio(15)
                .acumulable(Boolean.TRUE)
                .activa(Boolean.TRUE)
                .build();
        SaldoDiasEntity saldo = SaldoDiasEntity.builder()
                .id(1L)
                .colaboradorId(1001L)
                .politica(politica)
                .diasDisponibles(new BigDecimal("12.0"))
                .diasUsados(new BigDecimal("3.0"))
                .diasAcumulados(new BigDecimal("5.0"))
                .diasPendientes(new BigDecimal("0.0"))
                .diasTrabajados(359)
                .version(0)
                .build();

        when(saldoDiasRepository.findById(1L)).thenReturn(saldo);
        when(saldoDiasRepository.getEntityManager()).thenReturn(entityManager);

        SaldoDiasEntity resultado = saldoDiasWriteOperations.ejecutarProcesoDiario(1L);

        assertEquals(new BigDecimal("35.0"), resultado.getDiasAcumulados());
        assertEquals(0, resultado.getDiasTrabajados());
    }

    private SaldoDiasEntity buildSaldoDias(String diasDisponibles, String diasUsados, String diasAcumulados) {
        return SaldoDiasEntity.builder()
                .id(1L)
                .colaboradorId(1001L)
                .politica(PoliticaEntity.builder().id(1L).nombre("Vacaciones anuales").build())
                .diasDisponibles(new BigDecimal(diasDisponibles))
                .diasUsados(new BigDecimal(diasUsados))
                .diasAcumulados(new BigDecimal(diasAcumulados))
                .diasPendientes(BigDecimal.ZERO.setScale(1))
                .diasTrabajados(0)
                .version(0)
                .build();
    }
}
