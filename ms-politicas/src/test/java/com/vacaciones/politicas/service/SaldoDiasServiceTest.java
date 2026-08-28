package com.vacaciones.politicas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.exception.BadRequestException;
import com.vacaciones.politicas.exception.ResourceNotFoundException;
import com.vacaciones.politicas.exception.RuntimeCustomException;
import com.vacaciones.politicas.messaging.event.DiasDisponiblesActualizadosEvent;
import com.vacaciones.politicas.repository.PoliticaRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import org.eclipse.microprofile.reactive.messaging.Emitter;
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
    PoliticaRepository politicaRepository;

    @Mock
    SaldoDiasWriteOperations saldoDiasWriteOperations;

    @Mock
    EntityManager entityManager;

    @Mock
    Emitter<DiasDisponiblesActualizadosEvent> emitter;

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
        when(saldoDiasRepository.getEntityManager()).thenReturn(entityManager);

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
    void shouldPublishDiasDisponiblesActualizadosWithAsignacionMotivoWhenAsignarPoliticaSucceeds() {
        PoliticaEntity politica = PoliticaEntity.builder()
                .id(10L)
                .nombre("Vacaciones anuales")
                .diasBaseAnio(15)
                .activa(Boolean.TRUE)
                .build();

        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(null);
        when(politicaRepository.findById(10L)).thenReturn(politica);
        when(saldoDiasRepository.getEntityManager()).thenReturn(entityManager);

        saldoDiasService.asignarPolitica(1001L, 10L);

        ArgumentCaptor<DiasDisponiblesActualizadosEvent> captor =
                ArgumentCaptor.forClass(DiasDisponiblesActualizadosEvent.class);
        verify(emitter).send((DiasDisponiblesActualizadosEvent) captor.capture());

        DiasDisponiblesActualizadosEvent evento = captor.getValue();
        assertEquals(1001L, evento.colaboradorId());
        assertEquals(new BigDecimal("15.0"), evento.diasDisponibles());
        assertEquals(new BigDecimal("0.0"), evento.diasUsados());
        assertEquals("ASIGNACION_POLITICA", evento.motivoActualizacion());
        assertNotNull(evento.fechaEvento());
    }

    @Test
    void shouldNotPublishWhenAsignarPoliticaFailsBecausePoliticaDoesNotExist() {
        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(null);
        when(politicaRepository.findById(99L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> saldoDiasService.asignarPolitica(1001L, 99L));

        verify(emitter, never()).send(any(DiasDisponiblesActualizadosEvent.class));
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
        verify(emitter, never()).send(any(DiasDisponiblesActualizadosEvent.class));
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
        verify(emitter, never()).send(any(DiasDisponiblesActualizadosEvent.class));
    }

    @Test
    void shouldDelegateDescontarDiasToWriteOperations() {
        saldoDiasService.descontarDias(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.aprobada", "evt-aprobada-1");

        verify(saldoDiasWriteOperations).ejecutarDescuento(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.aprobada", "evt-aprobada-1");
    }

    @Test
    void shouldPublishDiasDisponiblesActualizadosWithDescuentoMotivoWhenDescuentoSucceeds() {
        SaldoDiasEntity saldo = buildSaldoDias("7.0", "5.0", "1.0");
        when(saldoDiasWriteOperations.ejecutarDescuento(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.aprobada", "evt-aprobada-1"))
                .thenReturn(saldo);

        saldoDiasService.descontarDias(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.aprobada", "evt-aprobada-1");

        ArgumentCaptor<DiasDisponiblesActualizadosEvent> captor =
                ArgumentCaptor.forClass(DiasDisponiblesActualizadosEvent.class);
        verify(emitter).send((DiasDisponiblesActualizadosEvent) captor.capture());

        DiasDisponiblesActualizadosEvent evento = captor.getValue();
        assertEquals(1001L, evento.colaboradorId());
        assertEquals(new BigDecimal("7.0"), evento.diasDisponibles());
        assertEquals(new BigDecimal("5.0"), evento.diasUsados());
        assertEquals("DESCUENTO_SOLICITUD_APROBADA", evento.motivoActualizacion());
        assertNotNull(evento.fechaEvento());
    }

    @Test
    void shouldNotPublishWhenDescuentoIsIdempotentNoOp() {
        when(saldoDiasWriteOperations.ejecutarDescuento(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.aprobada", "evt-duplicado"))
                .thenReturn(null);

        saldoDiasService.descontarDias(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.aprobada", "evt-duplicado");

        verify(emitter, never()).send(any(DiasDisponiblesActualizadosEvent.class));
    }

    @Test
    void shouldNotPublishWhenDescuentoFails() {
        when(saldoDiasWriteOperations.ejecutarDescuento(any(), any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Saldo no encontrado"));

        assertThrows(
                ResourceNotFoundException.class,
                () -> saldoDiasService.descontarDias(
                        9999L, 9001L, new BigDecimal("2.0"), "solicitud.aprobada", "evt-sin-saldo"));

        verify(emitter, never()).send(any(DiasDisponiblesActualizadosEvent.class));
    }

    @Test
    void shouldRetryOnceWhenOptimisticLockExceptionOccursOnDescuento() {
        SaldoDiasEntity saldo = buildSaldoDias("8.0", "4.0", "1.0");
        doThrow(new OptimisticLockException("conflicto de version"))
                .doReturn(saldo)
                .when(saldoDiasWriteOperations)
                .ejecutarDescuento(1001L, 9001L, new BigDecimal("2.0"), "solicitud.aprobada", "evt-concurrente-1");

        saldoDiasService.descontarDias(
                1001L, 9001L, new BigDecimal("2.0"), "solicitud.aprobada", "evt-concurrente-1");

        verify(saldoDiasWriteOperations, times(2)).ejecutarDescuento(
                1001L, 9001L, new BigDecimal("2.0"), "solicitud.aprobada", "evt-concurrente-1");
        verify(emitter).send(any(DiasDisponiblesActualizadosEvent.class));
    }

    @Test
    void shouldPropagateOptimisticLockExceptionAfterRetryOnDescuento() {
        doThrow(new OptimisticLockException("conflicto de version"))
                .when(saldoDiasWriteOperations)
                .ejecutarDescuento(1001L, 9001L, new BigDecimal("2.0"), "solicitud.aprobada", "evt-lock-fail");

        assertThrows(
                OptimisticLockException.class,
                () -> saldoDiasService.descontarDias(
                        1001L, 9001L, new BigDecimal("2.0"), "solicitud.aprobada", "evt-lock-fail"));

        verify(saldoDiasWriteOperations, times(2)).ejecutarDescuento(
                1001L, 9001L, new BigDecimal("2.0"), "solicitud.aprobada", "evt-lock-fail");
        verify(emitter, never()).send(any(DiasDisponiblesActualizadosEvent.class));
    }

    @Test
    void shouldDelegateDevolverDiasToWriteOperations() {
        saldoDiasService.devolverDias(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.cancelada", "evt-cancelada-1");

        verify(saldoDiasWriteOperations).ejecutarDevolucion(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.cancelada", "evt-cancelada-1");
    }

    @Test
    void shouldPublishDiasDisponiblesActualizadosWithDevolucionMotivoWhenDevolucionSucceeds() {
        SaldoDiasEntity saldo = buildSaldoDias("10.0", "2.0", "1.0");
        when(saldoDiasWriteOperations.ejecutarDevolucion(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.cancelada", "evt-cancelada-1"))
                .thenReturn(saldo);

        saldoDiasService.devolverDias(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.cancelada", "evt-cancelada-1");

        ArgumentCaptor<DiasDisponiblesActualizadosEvent> captor =
                ArgumentCaptor.forClass(DiasDisponiblesActualizadosEvent.class);
        verify(emitter).send((DiasDisponiblesActualizadosEvent) captor.capture());
        assertEquals("DEVOLUCION_SOLICITUD_CANCELADA", captor.getValue().motivoActualizacion());
    }

    @Test
    void shouldNotPublishWhenDevolucionIsIdempotentNoOp() {
        when(saldoDiasWriteOperations.ejecutarDevolucion(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.cancelada", "evt-duplicado"))
                .thenReturn(null);

        saldoDiasService.devolverDias(
                1001L, 9001L, new BigDecimal("3.0"), "solicitud.cancelada", "evt-duplicado");

        verify(emitter, never()).send(any(DiasDisponiblesActualizadosEvent.class));
    }

    @Test
    void shouldNotPublishWhenDevolucionFails() {
        when(saldoDiasWriteOperations.ejecutarDevolucion(any(), any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Saldo no encontrado"));

        assertThrows(
                ResourceNotFoundException.class,
                () -> saldoDiasService.devolverDias(
                        9999L, 9001L, new BigDecimal("2.0"), "solicitud.cancelada", "evt-sin-saldo"));

        verify(emitter, never()).send(any(DiasDisponiblesActualizadosEvent.class));
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
