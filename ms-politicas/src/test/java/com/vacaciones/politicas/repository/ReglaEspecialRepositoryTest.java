package com.vacaciones.politicas.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.entity.ReglaEspecialEntity;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ReglaEspecialRepositoryTest {

    @Inject
    PoliticaRepository politicaRepository;

    @Inject
    ReglaEspecialRepository reglaEspecialRepository;

    @Test
    @TestTransaction
    void shouldSaveAndFindReglaEspecialByIdWhenPoliticaExists() {
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

        ReglaEspecialEntity reglaEspecial = ReglaEspecialEntity.builder()
                .politica(politica)
                .condicion("ANTIGUEDAD_60_MESES")
                .diasAdicionales(5)
                .descripcion("Otorga 5 dias adicionales por antiguedad")
                .activa(Boolean.TRUE)
                .build();

        reglaEspecialRepository.persist(reglaEspecial);

        assertNotNull(reglaEspecial.getId());

        ReglaEspecialEntity persisted = reglaEspecialRepository.findById(reglaEspecial.getId());

        assertNotNull(persisted);
        assertEquals("ANTIGUEDAD_60_MESES", persisted.getCondicion());
        assertEquals(5, persisted.getDiasAdicionales());
        assertEquals(politica.getId(), persisted.getPolitica().getId());
    }
}