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
import java.time.LocalDate;
import java.util.List;
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

        saldoDiasService.asignarPolitica(1001L, 10L, 0);

        ArgumentCaptor<SaldoDiasEntity> saldoCaptor = ArgumentCaptor.forClass(SaldoDiasEntity.class);
        verify(saldoDiasRepository, times(2)).persist(saldoCaptor.capture());

        SaldoDiasEntity saldoPersistido = saldoCaptor.getValue();
        assertNotNull(saldoPersistido);
        assertEquals(1001L, saldoPersistido.getColaboradorId());
        assertEquals(10L, saldoPersistido.getPolitica().getId());
        assertEquals(new BigDecimal("15.0"), saldoPersistido.getDiasDisponibles());
        assertEquals(new BigDecimal("0.0"), saldoPersistido.getDiasUsados());
        assertEquals(new BigDecimal("0.0"), saldoPersistido.getDiasAcumulados());
        assertEquals(new BigDecimal("0.0"), saldoPersistido.getDiasPendientes());
        assertEquals(0, saldoPersistido.getDiasTrabajados());
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

        saldoDiasService.asignarPolitica(1001L, 10L, null);

        ArgumentCaptor<DiasDisponiblesActualizadosEvent> captor =
                ArgumentCaptor.forClass(DiasDisponiblesActualizadosEvent.class);
        verify(emitter).send((DiasDisponiblesActualizadosEvent) captor.capture());

        DiasDisponiblesActualizadosEvent evento = captor.getValue();
        assertEquals(1001L, evento.colaboradorId());
        assertEquals(new BigDecimal("15.0"), evento.diasDisponibles());
        assertEquals(new BigDecimal("0.0"), evento.diasUsados());
        assertEquals(new BigDecimal("15.0"), evento.diasHabilitados());
        assertEquals(new BigDecimal("15.0"), evento.saldoActual());
        assertEquals(new BigDecimal("0.0"), evento.diasTruncos());
        assertEquals(0, evento.diasTrabajados());
        assertEquals(new BigDecimal("0.0"), evento.diasPendientes());
        assertEquals("ASIGNACION_POLITICA", evento.motivoActualizacion());
        assertNotNull(evento.fechaEvento());
    }

    @Test
    void shouldNotPublishWhenAsignarPoliticaFailsBecausePoliticaDoesNotExist() {
        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(null);
        when(politicaRepository.findById(99L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> saldoDiasService.asignarPolitica(1001L, 99L, 0));

        verify(emitter, never()).send(any(DiasDisponiblesActualizadosEvent.class));
    }

    @Test
    void shouldRejectAsignarPoliticaWhenColaboradorAlreadyHasAssignedPolicy() {
        when(saldoDiasRepository.findByColaboradorId(1001L))
                .thenReturn(buildSaldoDias("10.0", "2.0", "1.0"));

        RuntimeCustomException thrown = assertThrows(
                RuntimeCustomException.class,
                () -> saldoDiasService.asignarPolitica(1001L, 10L, 0));

        assertEquals(Response.Status.CONFLICT, thrown.getStatus());
        verify(politicaRepository, never()).findById(any());
        verify(saldoDiasRepository, never()).persist(any(SaldoDiasEntity.class));
        verify(emitter, never()).send(any(DiasDisponiblesActualizadosEvent.class));
    }

    @Test
    void shouldRejectAsignarPoliticaWhenPoliticaDoesNotExist() {
        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(null);
        when(politicaRepository.findById(99L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> saldoDiasService.asignarPolitica(1001L, 99L, 0));

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

        assertThrows(BadRequestException.class, () -> saldoDiasService.asignarPolitica(1001L, 11L, 12));

        verify(saldoDiasRepository, never()).persist(any(SaldoDiasEntity.class));
        verify(emitter, never()).send(any(DiasDisponiblesActualizadosEvent.class));
    }

    @Test
    void shouldRejectAsignarPoliticaWhenColaboradorDoesNotMeetAntiguedadMinima() {
        PoliticaEntity politicaPremium = PoliticaEntity.builder()
                .id(12L)
                .nombre("Vacaciones premium")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(20)
                .antiguedadMinimaMeses(12)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(40)
                .activa(Boolean.TRUE)
                .build();

        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(null);
        when(politicaRepository.findById(12L)).thenReturn(politicaPremium);

        assertThrows(BadRequestException.class, () -> saldoDiasService.asignarPolitica(1001L, 12L, 6));

        verify(saldoDiasRepository, never()).persist(any(SaldoDiasEntity.class));
        verify(emitter, never()).send(any(DiasDisponiblesActualizadosEvent.class));
    }

    @Test
    void shouldRejectAsignarPoliticaWhenAntiguedadMesesIsNullAndPoliticaRequiresAntiguedad() {
        PoliticaEntity politicaPremium = PoliticaEntity.builder()
                .id(12L)
                .nombre("Vacaciones premium")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(20)
                .antiguedadMinimaMeses(12)
                .activa(Boolean.TRUE)
                .build();

        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(null);
        when(politicaRepository.findById(12L)).thenReturn(politicaPremium);

        assertThrows(BadRequestException.class, () -> saldoDiasService.asignarPolitica(1001L, 12L, null));

        verify(saldoDiasRepository, never()).persist(any(SaldoDiasEntity.class));
    }

    @Test
    void shouldAllowAsignarPoliticaWhenColaboradorMeetsAntiguedadMinimaExactly() {
        PoliticaEntity politicaPremium = PoliticaEntity.builder()
                .id(12L)
                .nombre("Vacaciones premium")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(20)
                .antiguedadMinimaMeses(12)
                .activa(Boolean.TRUE)
                .build();

        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(null);
        when(politicaRepository.findById(12L)).thenReturn(politicaPremium);
        when(saldoDiasRepository.getEntityManager()).thenReturn(entityManager);

        saldoDiasService.asignarPolitica(1001L, 12L, 12);

        verify(saldoDiasRepository, times(2)).persist(any(SaldoDiasEntity.class));
    }

    @Test
    void shouldAllowAsignarPoliticaWhenPoliticaHasNoAntiguedadRequirementAndAntiguedadMesesIsNull() {
        PoliticaEntity politicaSinRequisito = PoliticaEntity.builder()
                .id(13L)
                .nombre("Vacaciones base")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(15)
                .antiguedadMinimaMeses(0)
                .activa(Boolean.TRUE)
                .build();

        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(null);
        when(politicaRepository.findById(13L)).thenReturn(politicaSinRequisito);
        when(saldoDiasRepository.getEntityManager()).thenReturn(entityManager);

        saldoDiasService.asignarPolitica(1001L, 13L, null);

        verify(saldoDiasRepository, times(2)).persist(any(SaldoDiasEntity.class));
    }

    @Test
    void shouldAssignDefaultPoliticaWhenColaboradorHasNoSaldo() {
        PoliticaEntity politicaPorDefecto = PoliticaEntity.builder()
                .id(20L)
                .nombre("Politica estandar")
                .diasBaseAnio(30)
                .activa(Boolean.TRUE)
                .esPorDefecto(Boolean.TRUE)
                .build();

        when(saldoDiasRepository.findByColaboradorId(2001L)).thenReturn(null);
        when(politicaRepository.findByEsPorDefectoTrue()).thenReturn(politicaPorDefecto);
        when(politicaRepository.findById(20L)).thenReturn(politicaPorDefecto);
        when(saldoDiasRepository.getEntityManager()).thenReturn(entityManager);

        saldoDiasService.asignarPoliticaPorDefectoSiNoTiene(2001L);

        ArgumentCaptor<SaldoDiasEntity> captor = ArgumentCaptor.forClass(SaldoDiasEntity.class);
        verify(saldoDiasRepository, times(2)).persist(captor.capture());
        assertEquals(new BigDecimal("30.0"), captor.getValue().getDiasDisponibles());
        assertEquals(20L, captor.getValue().getPolitica().getId());
    }

    @Test
    void shouldDoNothingWhenColaboradorAlreadyHasSaldoAssigned() {
        when(saldoDiasRepository.findByColaboradorId(2001L)).thenReturn(buildSaldoDias("30.0", "0.0", "0.0"));

        saldoDiasService.asignarPoliticaPorDefectoSiNoTiene(2001L);

        verify(politicaRepository, never()).findByEsPorDefectoTrue();
        verify(saldoDiasRepository, never()).persist(any(SaldoDiasEntity.class));
    }

    @Test
    void shouldThrowWhenNoDefaultPoliticaIsConfigured() {
        when(saldoDiasRepository.findByColaboradorId(2001L)).thenReturn(null);
        when(politicaRepository.findByEsPorDefectoTrue()).thenReturn(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> saldoDiasService.asignarPoliticaPorDefectoSiNoTiene(2001L));

        verify(saldoDiasRepository, never()).persist(any(SaldoDiasEntity.class));
    }

    @Test
        void shouldPublishDiasActualizadosForEachSuccessfulBatchDiario() {
        SaldoDiasEntity saldo = buildSaldoDias("15.0", "0.0", "8.0");

                when(saldoDiasRepository.findParaProcesoDiario()).thenReturn(List.of(saldo));
                when(saldoDiasWriteOperations.ejecutarProcesoDiario(saldo)).thenReturn(saldo);

        saldoDiasService.renovarSaldosAcumulablesDelDia(LocalDate.of(2026, 8, 15));

                verify(saldoDiasWriteOperations).ejecutarProcesoDiario(saldo);

        ArgumentCaptor<DiasDisponiblesActualizadosEvent> captor =
                ArgumentCaptor.forClass(DiasDisponiblesActualizadosEvent.class);
        verify(emitter).send((DiasDisponiblesActualizadosEvent) captor.capture());
        assertEquals("BATCH_DIARIO", captor.getValue().motivoActualizacion());
    }

    @Test
    void shouldContinueProcessingOtherSaldosWhenOneBatchProcessFails() {
        PoliticaEntity politica = PoliticaEntity.builder().id(1L).nombre("Vacaciones anuales").build();
        SaldoDiasEntity saldoFalla = SaldoDiasEntity.builder()
                .id(1L).colaboradorId(1001L).politica(politica)
                .diasDisponibles(new BigDecimal("5.0")).diasUsados(new BigDecimal("0.0"))
                .diasAcumulados(new BigDecimal("0.0")).diasPendientes(new BigDecimal("0.0")).diasTrabajados(5)
                .version(0).build();
        SaldoDiasEntity saldoOk = SaldoDiasEntity.builder()
                .id(2L).colaboradorId(1002L).politica(politica)
                .diasDisponibles(new BigDecimal("5.0")).diasUsados(new BigDecimal("0.0"))
                .diasAcumulados(new BigDecimal("0.0")).diasPendientes(new BigDecimal("0.0")).diasTrabajados(5)
                .version(0).build();

        when(saldoDiasRepository.findParaProcesoDiario()).thenReturn(List.of(saldoFalla, saldoOk));
        when(saldoDiasWriteOperations.ejecutarProcesoDiario(saldoFalla))
                .thenThrow(new RuntimeException("fallo inesperado"));
        when(saldoDiasWriteOperations.ejecutarProcesoDiario(saldoOk)).thenReturn(saldoOk);

        saldoDiasService.renovarSaldosAcumulablesDelDia(LocalDate.of(2026, 8, 15));

                verify(saldoDiasWriteOperations).ejecutarProcesoDiario(saldoFalla);
                verify(saldoDiasWriteOperations).ejecutarProcesoDiario(saldoOk);
        verify(emitter, times(1)).send(any(DiasDisponiblesActualizadosEvent.class));
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
        assertEquals(new BigDecimal("25.0"), evento.diasDisponibles());
        assertEquals(new BigDecimal("5.0"), evento.diasUsados());
                assertEquals(new BigDecimal("1.0"), evento.diasPendientes());
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

    @Test
    void shouldCalculateDiasTruncosWithBusinessFormula() {
        // Arrange: Preparamos la política y la entidad con 180 días trabajados
        PoliticaEntity politica = PoliticaEntity.builder()
                .diasBaseAnio(30)
                .build();

        SaldoDiasEntity saldo = SaldoDiasEntity.builder()
                .politica(politica)
                .diasTrabajados(180)
                .build();

        // Act
        BigDecimal truncos = saldoDiasService.calcularDiasTruncos(saldo);

        // Assert: 180 días de 360 con base 30 equivalen a 15.00 días truncos
        assertEquals(0, new BigDecimal("15.00").compareTo(truncos));
    }

    @Test
    void shouldCalculateSaldoActualAsHabilitadosMinusGozadosMinusPendientes() {
        // Arrange: Preparamos la política y la entidad con los valores acumulados/usados
        PoliticaEntity politica = PoliticaEntity.builder()
                .diasBaseAnio(12)
                .antiguedadMinimaMeses(0) // Para que no bloquee el saldo por antigüedad
                .build();

        SaldoDiasEntity saldo = SaldoDiasEntity.builder()
                .politica(politica)
                .diasAcumulados(new BigDecimal("5.00"))  // Reemplaza los 5.0 base
                .diasUsados(new BigDecimal("5.00"))      // Gozados
                .diasPendientes(new BigDecimal("3.00"))  // Pendientes
                .diasTrabajados(360)                     // 1 periodo completado (12 días ganados)
                .fechaIngresoColaborador(LocalDate.now().minusYears(1))
                .build();

        // Act
        BigDecimal habilitados = saldoDiasService.calcularDiasHabilitados(saldo);
        BigDecimal saldoActual = saldoDiasService.calcularSaldoActual(saldo, habilitados);

        // Assert:
        // Habilitados = 12 (1 año cumplido) + 5 (acumulados) = 17.00
        // Saldo Actual = 17.00 - 5.00 (usados) - 3.00 (pendientes) = 9.00
        assertEquals(0, new BigDecimal("17.00").compareTo(habilitados));
        assertEquals(0, new BigDecimal("9.00").compareTo(saldoActual));
    }

        @Test
        void shouldPublishRenovacionMotivoWhenWorkerCompletes360Days() {
                SaldoDiasEntity saldoAntes = buildSaldoDias("12.0", "2.0", "5.0");
                saldoAntes.setDiasTrabajados(359);
                SaldoDiasEntity saldoDespues = buildSaldoDias("12.0", "2.0", "35.0");
                saldoDespues.setDiasTrabajados(0);

                when(saldoDiasRepository.findParaProcesoDiario()).thenReturn(List.of(saldoAntes));
                when(saldoDiasWriteOperations.ejecutarProcesoDiario(saldoAntes)).thenReturn(saldoDespues);

                saldoDiasService.renovarSaldosAcumulablesDelDia(LocalDate.of(2026, 9, 9));

                ArgumentCaptor<DiasDisponiblesActualizadosEvent> captor =
                                ArgumentCaptor.forClass(DiasDisponiblesActualizadosEvent.class);
                verify(emitter).send((DiasDisponiblesActualizadosEvent) captor.capture());
                assertEquals("RENOVACION_PERIODO_ACUMULACION", captor.getValue().motivoActualizacion());
        }

    private SaldoDiasEntity buildSaldoDias(String diasDisponibles, String diasUsados, String diasAcumulados) {
        return SaldoDiasEntity.builder()
                .id(1L)
                .colaboradorId(1001L)
                                .politica(PoliticaEntity.builder().id(1L).nombre("Vacaciones anuales").diasBaseAnio(15).build())
                .diasDisponibles(new BigDecimal(diasDisponibles))
                .diasUsados(new BigDecimal(diasUsados))
                .diasAcumulados(new BigDecimal(diasAcumulados))
                                .diasPendientes(new BigDecimal("1.0"))
                                .diasTrabajados(180)
                .version(0)
                .build();
    }
}
