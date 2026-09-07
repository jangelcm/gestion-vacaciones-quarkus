package com.vacaciones.politicas.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.repository.PoliticaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PoliticaDefaultSeederTest {

    @Mock
    PoliticaRepository politicaRepository;

    @InjectMocks
    PoliticaDefaultSeeder seeder;

    @Test
    void shouldCreateDefaultPoliticaWithThirtyDaysWhenNoneExists() {
        when(politicaRepository.findByEsPorDefectoTrue()).thenReturn(null);

        seeder.onStart(null);

        ArgumentCaptor<PoliticaEntity> captor = ArgumentCaptor.forClass(PoliticaEntity.class);
        verify(politicaRepository).persist(captor.capture());

        PoliticaEntity persistida = captor.getValue();
        assertEquals(30, persistida.getDiasBaseAnio());
        assertTrue(persistida.getEsPorDefecto());
        assertTrue(persistida.getActiva());
    }

    @Test
    void shouldNotCreateDefaultPoliticaWhenOneAlreadyExists() {
        when(politicaRepository.findByEsPorDefectoTrue())
                .thenReturn(PoliticaEntity.builder().id(1L).esPorDefecto(true).build());

        seeder.onStart(null);

        verify(politicaRepository, never()).persist(any(PoliticaEntity.class));
    }
}
