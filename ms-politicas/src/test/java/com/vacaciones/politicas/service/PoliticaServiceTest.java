package com.vacaciones.politicas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vacaciones.politicas.dto.request.PoliticaRequestDto;
import com.vacaciones.politicas.dto.response.PoliticaResponseDto;
import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.exception.ResourceNotFoundException;
import com.vacaciones.politicas.messaging.event.PoliticaActualizadaEvent;
import com.vacaciones.politicas.repository.PoliticaRepository;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PoliticaServiceTest {

    @Mock
    PoliticaRepository politicaRepository;

    @Mock
    EntityManager entityManager;

    @Mock
    Emitter<PoliticaActualizadaEvent> politicaActualizadaEmitter;

    @InjectMocks
    PoliticaService politicaService;

    @Test
    void shouldSavePoliticaWhenRequestIsValid() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 15, 10, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 15, 10, 0, 0);
        PoliticaRequestDto request = buildRequestDto();

        doAnswer(invocation -> {
            PoliticaEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setCreatedAt(createdAt);
            entity.setUpdatedAt(updatedAt);
            return null;
        }).when(politicaRepository).persist(any(PoliticaEntity.class));
        when(politicaRepository.getEntityManager()).thenReturn(entityManager);

        PoliticaResponseDto response = politicaService.save(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Vacaciones anuales", response.nombre());
        assertEquals("ANUAL", response.tipoVacacion());
        assertEquals("15-08-2026 10:00:00", response.createdAt());
        verify(politicaRepository).persist(any(PoliticaEntity.class));
    }

    @Test
    void shouldNotEmitEventWhenPersistFailsOnSave() {
        PoliticaRequestDto request = buildRequestDto();

        doThrow(new RuntimeException("fallo de persistencia"))
                .when(politicaRepository).persist(any(PoliticaEntity.class));

        assertThrows(RuntimeException.class, () -> politicaService.save(request));

        verify(politicaActualizadaEmitter, never()).send(any(PoliticaActualizadaEvent.class));
    }

    @Test
    void shouldEmitPoliticaActualizadaEventWhenSaveSucceeds() {
        LocalDateTime antes = LocalDateTime.now();
        PoliticaRequestDto request = buildRequestDto();

        doAnswer(invocation -> {
            PoliticaEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return null;
        }).when(politicaRepository).persist(any(PoliticaEntity.class));
        when(politicaRepository.getEntityManager()).thenReturn(entityManager);

        politicaService.save(request);
        LocalDateTime despues = LocalDateTime.now();

        ArgumentCaptor<PoliticaActualizadaEvent> captor = ArgumentCaptor.forClass(PoliticaActualizadaEvent.class);
        verify(politicaActualizadaEmitter).send((PoliticaActualizadaEvent) captor.capture());

        PoliticaActualizadaEvent evento = captor.getValue();
        assertEquals(1L, evento.politicaId());
        assertEquals("Vacaciones anuales", evento.nombre());
        assertEquals("ANUAL", evento.tipoVacacion());
        assertEquals(15, evento.diasBaseAnio());
        assertEquals(Boolean.TRUE, evento.activa());
        assertNotNull(evento.fechaEvento());
        assertFalse(evento.fechaEvento().isBefore(antes));
        assertFalse(evento.fechaEvento().isAfter(despues));
    }

    @Test
    void shouldUpdatePoliticaWhenItExists() {
        PoliticaEntity existing = buildEntity(1L, "Vacaciones base", "ANUAL", 15, 0, true, 30, true);
        existing.setCreatedAt(LocalDateTime.of(2026, 8, 15, 9, 0, 0));
        existing.setUpdatedAt(LocalDateTime.of(2026, 8, 15, 9, 0, 0));
        PoliticaRequestDto request = new PoliticaRequestDto(
                "Vacaciones premium",
                "ANUAL",
                20,
                12,
                Boolean.TRUE,
                40,
                Boolean.TRUE);

        when(politicaRepository.findById(1L)).thenReturn(existing);
        when(politicaRepository.getEntityManager()).thenReturn(entityManager);

        PoliticaResponseDto response = politicaService.update(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Vacaciones premium", response.nombre());
        assertEquals(20, response.diasBaseAnio());
        assertEquals(12, response.antiguedadMinimaMeses());
        assertEquals(40, response.maxDiasAcumulables());
        verify(politicaRepository).findById(1L);
    }

    @Test
    void shouldEmitPoliticaActualizadaEventWithUpdatedDataWhenUpdateSucceeds() {
        PoliticaEntity existing = buildEntity(1L, "Vacaciones base", "ANUAL", 15, 0, true, 30, true);
        PoliticaRequestDto request = new PoliticaRequestDto(
                "Vacaciones premium",
                "ANUAL",
                20,
                12,
                Boolean.TRUE,
                40,
                Boolean.FALSE);

        when(politicaRepository.findById(1L)).thenReturn(existing);
        when(politicaRepository.getEntityManager()).thenReturn(entityManager);

        politicaService.update(1L, request);

        ArgumentCaptor<PoliticaActualizadaEvent> captor = ArgumentCaptor.forClass(PoliticaActualizadaEvent.class);
        verify(politicaActualizadaEmitter).send((PoliticaActualizadaEvent) captor.capture());

        PoliticaActualizadaEvent evento = captor.getValue();
        assertEquals(1L, evento.politicaId());
        assertEquals("Vacaciones premium", evento.nombre());
        assertEquals(20, evento.diasBaseAnio());
        assertEquals(Boolean.FALSE, evento.activa());
    }

    @Test
    void shouldNotEmitEventWhenUpdateFailsBecausePoliticaDoesNotExist() {
        PoliticaRequestDto request = buildRequestDto();
        when(politicaRepository.findById(99L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> politicaService.update(99L, request));

        verify(politicaActualizadaEmitter, never()).send(any(PoliticaActualizadaEvent.class));
    }

    @Test
    void shouldThrowWhenUpdatingPoliticaThatDoesNotExist() {
        PoliticaRequestDto request = buildRequestDto();

        when(politicaRepository.findById(99L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> politicaService.update(99L, request));
    }

    @Test
    void shouldFindPoliticaByIdWhenItExists() {
        PoliticaEntity existing = buildEntity(1L, "Vacaciones anuales", "ANUAL", 15, 0, true, 30, true);
        existing.setCreatedAt(LocalDateTime.of(2026, 8, 15, 8, 0, 0));
        existing.setUpdatedAt(LocalDateTime.of(2026, 8, 15, 8, 30, 0));

        when(politicaRepository.findById(1L)).thenReturn(existing);

        PoliticaResponseDto response = politicaService.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Vacaciones anuales", response.nombre());
        assertEquals("15-08-2026 08:00:00", response.createdAt());
        assertEquals("15-08-2026 08:30:00", response.updatedAt());
    }

    @Test
    void shouldThrowWhenFindingPoliticaByIdThatDoesNotExist() {
        when(politicaRepository.findById(99L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> politicaService.findById(99L));
    }

    @Test
    void shouldDeletePoliticaWhenItExists() {
        PoliticaEntity existing = buildEntity(1L, "Vacaciones anuales", "ANUAL", 15, 0, true, 30, true);
        existing.setCreatedAt(LocalDateTime.of(2026, 8, 15, 8, 0, 0));
        existing.setUpdatedAt(LocalDateTime.of(2026, 8, 15, 8, 30, 0));

        when(politicaRepository.findById(1L)).thenReturn(existing);
        when(politicaRepository.deleteById(1L)).thenReturn(true);

        PoliticaResponseDto response = politicaService.delete(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Vacaciones anuales", response.nombre());
        verify(politicaRepository).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDeletingPoliticaThatDoesNotExist() {
        when(politicaRepository.findById(77L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> politicaService.delete(77L));
    }

    @Test
    void shouldReturnAllPoliticas() {
        PoliticaEntity first = buildEntity(1L, "Vacaciones anuales", "ANUAL", 15, 0, true, 30, true);
        first.setCreatedAt(LocalDateTime.of(2026, 8, 15, 7, 0, 0));
        first.setUpdatedAt(LocalDateTime.of(2026, 8, 15, 7, 10, 0));
        PoliticaEntity second = buildEntity(2L, "Vacaciones premium", "ANUAL", 20, 12, true, 40, true);
        second.setCreatedAt(LocalDateTime.of(2026, 8, 15, 7, 20, 0));
        second.setUpdatedAt(LocalDateTime.of(2026, 8, 15, 7, 30, 0));

        when(politicaRepository.listAll()).thenReturn(List.of(first, second));

        List<PoliticaResponseDto> responses = politicaService.getAll();

        assertEquals(2, responses.size());
        assertEquals("Vacaciones anuales", responses.get(0).nombre());
        assertEquals("Vacaciones premium", responses.get(1).nombre());
        verify(politicaRepository).listAll();
    }

    private PoliticaRequestDto buildRequestDto() {
        return new PoliticaRequestDto(
                "Vacaciones anuales",
                "ANUAL",
                15,
                0,
                Boolean.TRUE,
                30,
                Boolean.TRUE);
    }

    private PoliticaEntity buildEntity(
            Long id,
            String nombre,
            String tipoVacacion,
            Integer diasBaseAnio,
            Integer antiguedadMinimaMeses,
            Boolean acumulable,
            Integer maxDiasAcumulables,
            Boolean activa) {
        return PoliticaEntity.builder()
                .id(id)
                .nombre(nombre)
                .tipoVacacion(tipoVacacion)
                .diasBaseAnio(diasBaseAnio)
                .antiguedadMinimaMeses(antiguedadMinimaMeses)
                .acumulable(acumulable)
                .maxDiasAcumulables(maxDiasAcumulables)
                .activa(activa)
                .build();
    }
}