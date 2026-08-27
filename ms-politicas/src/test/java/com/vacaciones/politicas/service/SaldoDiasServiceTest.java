package com.vacaciones.politicas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vacaciones.politicas.entity.MovimientoSaldoEntity;
import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.exception.BadRequestException;
import com.vacaciones.politicas.exception.ResourceNotFoundException;
import com.vacaciones.politicas.exception.RuntimeCustomException;
import com.vacaciones.politicas.repository.MovimientoSaldoRepository;
import com.vacaciones.politicas.repository.PoliticaRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaldoDiasServiceTest {

    @Mock
    SaldoDiasRepository saldoDiasRepository;

    @Mock
    MovimientoSaldoRepository movimientoSaldoRepository;

    @Mock
    PoliticaRepository politicaRepository;

    @InjectMocks
    SaldoDiasService saldoDiasService;

    @Test
    void shouldAsignarPoliticaAndCreateSaldoDiasWithDiasDisponiblesFromPolitica() {
        PoliticaEntity politica = PoliticaEntity.builder()
                .id(10L)
                .nombre("Vacaciones anuales")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(15)
                .antiguedadMinimaMeses(0)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(30)
                .activa(Boolean.TRUE)
                .build();

        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(null);
        when(politicaRepository.findById(10L)).thenReturn(politica);
        doNothing().when(saldoDiasRepository).persist(any(SaldoDiasEntity.class));

        saldoDiasService.asignarPolitica(1001L, 10L);

        ArgumentCaptor<SaldoDiasEntity> saldoCaptor = ArgumentCaptor.forClass(SaldoDiasEntity.class);
        verify(saldoDiasRepository).persist(saldoCaptor.capture());

        SaldoDiasEntity saldoPersistido = saldoCaptor.getValue();
        assertNotNull(saldoPersistido);
        assertEquals(1001L, saldoPersistido.getColaboradorId());
        assertEquals(10L, saldoPersistido.getPolitica().getId());
        assertEquals(new BigDecimal("15.0"), saldoPersistido.getDiasDisponibles());
        assertEquals(new BigDecimal("0.0"), saldoPersistido.getDiasUsados());
        assertEquals(new BigDecimal("0.0"), saldoPersistido.getDiasAcumulados());
    }

    @Test
    void shouldRejectAsignarPoliticaWhenColaboradorAlreadyHasAssignedPolicy() {
        when(saldoDiasRepository.findByColaboradorId(1001L))
                .thenReturn(buildSaldoDias("10.0", "2.0", "1.0"));

        RuntimeCustomException thrown = assertThrows(
            RuntimeCustomException.class,
            () -> saldoDiasService.asignarPolitica(1001L, 10L));

        assertEquals(Response.Status.CONFLICT, thrown.getStatus());
        verify(politicaRepository, never()).findById(any());
        verify(saldoDiasRepository, never()).persist(any(SaldoDiasEntity.class));
    }

    @Test
    void shouldRejectAsignarPoliticaWhenPoliticaDoesNotExist() {
        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(null);
        when(politicaRepository.findById(99L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> saldoDiasService.asignarPolitica(1001L, 99L));

        verify(saldoDiasRepository, never()).persist(any(SaldoDiasEntity.class));
    }

    @Test
    void shouldRejectAsignarPoliticaWhenPoliticaIsInactive() {
        PoliticaEntity politicaInactiva = PoliticaEntity.builder()
                .id(11L)
                .nombre("Vacaciones inactiva")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(20)
                .antiguedadMinimaMeses(12)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(40)
                .activa(Boolean.FALSE)
                .build();

        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(null);
        when(politicaRepository.findById(11L)).thenReturn(politicaInactiva);

        assertThrows(BadRequestException.class, () -> saldoDiasService.asignarPolitica(1001L, 11L));

        verify(saldoDiasRepository, never()).persist(any(SaldoDiasEntity.class));
    }

    @Test
    void shouldDescontarDiasAndCreateMovimientoWhenEventoIdIsNew() {
        SaldoDiasEntity saldoDias = buildSaldoDias("10.0", "2.0", "1.0");

        when(movimientoSaldoRepository.existsByEventoId("evt-aprobada-1")).thenReturn(false);
        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(saldoDias);
        doNothing().when(movimientoSaldoRepository).persist(any(MovimientoSaldoEntity.class));

        saldoDiasService.descontarDias(1001L, 9001L, new BigDecimal("3.0"), "solicitud.aprobada", "evt-aprobada-1");

        assertEquals(new BigDecimal("7.0"), saldoDias.getDiasDisponibles());
        assertEquals(new BigDecimal("5.0"), saldoDias.getDiasUsados());

        ArgumentCaptor<MovimientoSaldoEntity> movimientoCaptor = ArgumentCaptor.forClass(MovimientoSaldoEntity.class);
        verify(movimientoSaldoRepository).persist(movimientoCaptor.capture());
        assertEquals("evt-aprobada-1", movimientoCaptor.getValue().getEventoId());
        assertEquals(saldoDias.getId(), movimientoCaptor.getValue().getSaldo().getId());
        assertEquals("APROBACION", movimientoCaptor.getValue().getTipoMovimiento());
    }

    @Test
    void shouldDoNothingWhenEventoIdAlreadyExists() {
        SaldoDiasEntity saldoDias = buildSaldoDias("10.0", "2.0", "1.0");

        when(movimientoSaldoRepository.existsByEventoId("evt-duplicado-1")).thenReturn(true);

        saldoDiasService.descontarDias(1001L, 9001L, new BigDecimal("3.0"), "solicitud.aprobada", "evt-duplicado-1");

        assertEquals(new BigDecimal("10.0"), saldoDias.getDiasDisponibles());
        assertEquals(new BigDecimal("2.0"), saldoDias.getDiasUsados());
        verify(saldoDiasRepository, never()).findByColaboradorId(any());
        verify(saldoDiasRepository, never()).persist(any(SaldoDiasEntity.class));
        verify(movimientoSaldoRepository, never()).persist(any(MovimientoSaldoEntity.class));
    }

    @Test
    void shouldDevolverDiasAndCreateMovimientoWhenEventoIdIsNew() {
        SaldoDiasEntity saldoDias = buildSaldoDias("7.0", "5.0", "1.0");

        when(movimientoSaldoRepository.existsByEventoId("evt-cancelada-1")).thenReturn(false);
        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(saldoDias);
        doNothing().when(movimientoSaldoRepository).persist(any(MovimientoSaldoEntity.class));

        saldoDiasService.devolverDias(1001L, 9001L, new BigDecimal("3.0"), "solicitud.cancelada", "evt-cancelada-1");

        assertEquals(new BigDecimal("10.0"), saldoDias.getDiasDisponibles());
        assertEquals(new BigDecimal("2.0"), saldoDias.getDiasUsados());

        ArgumentCaptor<MovimientoSaldoEntity> movimientoCaptor = ArgumentCaptor.forClass(MovimientoSaldoEntity.class);
        verify(movimientoSaldoRepository).persist(movimientoCaptor.capture());
        assertEquals("evt-cancelada-1", movimientoCaptor.getValue().getEventoId());
        assertEquals("CANCELACION", movimientoCaptor.getValue().getTipoMovimiento());
    }

    @Test
    void shouldThrowOptimisticLockExceptionWhenConcurrentUpdateOccurs() {
        SaldoDiasEntity saldoDias = buildSaldoDias("10.0", "2.0", "1.0");

        when(movimientoSaldoRepository.existsByEventoId("evt-concurrente-1")).thenReturn(false);
        when(movimientoSaldoRepository.existsByEventoId("evt-concurrente-2")).thenReturn(false);
        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(saldoDias);
        doNothing().when(movimientoSaldoRepository).persist(any(MovimientoSaldoEntity.class));
        doThrow(new OptimisticLockException("conflicto de version"))
                .when(saldoDiasRepository).persist(eq(saldoDias));

        saldoDiasService.descontarDias(1001L, 9001L, new BigDecimal("2.0"), "solicitud.aprobada", "evt-concurrente-1");

        assertThrows(
                OptimisticLockException.class,
                () -> saldoDiasService.descontarDias(
                        1001L,
                        9002L,
                        new BigDecimal("1.0"),
                        "solicitud.aprobada",
                        "evt-concurrente-2"));

        verify(movimientoSaldoRepository, times(1)).persist(any(MovimientoSaldoEntity.class));
    }

    private SaldoDiasEntity buildSaldoDias(String diasDisponibles, String diasUsados, String diasAcumulados) {
        return SaldoDiasEntity.builder()
                .id(1L)
                .colaboradorId(1001L)
                .politica(PoliticaEntity.builder().id(1L).nombre("Vacaciones anuales").build())
                .diasDisponibles(new BigDecimal(diasDisponibles))
                .diasUsados(new BigDecimal(diasUsados))
                .diasAcumulados(new BigDecimal(diasAcumulados))
                .version(0)
                .build();
    }
}