package com.vacaciones.politicas.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.vacaciones.politicas.entity.PoliticaEntity;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PoliticaRepositoryTest {

    @Inject
    PoliticaRepository politicaRepository;

    @Test
    @TestTransaction
    void shouldSaveAndFindPoliticaById() {
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

        assertNotNull(politica.getId());

        PoliticaEntity persisted = politicaRepository.findById(politica.getId());

        assertNotNull(persisted);
        assertEquals("Vacaciones anuales", persisted.getNombre());
        assertEquals("ANUAL", persisted.getTipoVacacion());
    }
}