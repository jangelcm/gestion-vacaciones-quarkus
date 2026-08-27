package com.vacaciones.politicas.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SaldoDiasRepositoryTest {

    @Inject
    PoliticaRepository politicaRepository;

    @Inject
    SaldoDiasRepository saldoDiasRepository;

    @Test
    @TestTransaction
    void shouldSaveAndFindSaldoDiasById() {
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
                .colaboradorId(1001L)
                .politica(politica)
                .diasDisponibles(new BigDecimal("15.0"))
                .diasUsados(new BigDecimal("0.0"))
                .diasAcumulados(new BigDecimal("0.0"))
                .build();

        saldoDiasRepository.persist(saldoDias);

        assertNotNull(saldoDias.getId());

        SaldoDiasEntity persisted = saldoDiasRepository.findById(saldoDias.getId());

        assertNotNull(persisted);
        assertEquals(1001L, persisted.getColaboradorId());
        assertEquals(new BigDecimal("15.0"), persisted.getDiasDisponibles());
    }

    @Test
    @TestTransaction
    void shouldFindSaldoDiasByColaboradorIdAndReturnNullWhenNotExists() {
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
                .colaboradorId(2002L)
                .politica(politica)
                .diasDisponibles(new BigDecimal("20.0"))
                .diasUsados(new BigDecimal("2.0"))
                .diasAcumulados(new BigDecimal("1.0"))
                .build();

        saldoDiasRepository.persist(saldoDias);

        SaldoDiasEntity found = saldoDiasRepository.findByColaboradorId(2002L);

        assertNotNull(found);
        assertEquals(saldoDias.getId(), found.getId());
        assertEquals(2002L, found.getColaboradorId());

        SaldoDiasEntity missing = saldoDiasRepository.findByColaboradorId(9999L);

        assertNull(missing);
    }

    @Test
    @TestTransaction
    void shouldFindAllSaldoDiasByPoliticaIdAndReturnEmptyListWhenNoAssignmentsExist() {
        PoliticaEntity politicaAnual = PoliticaEntity.builder()
                .nombre("Vacaciones anuales")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(15)
                .antiguedadMinimaMeses(0)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(30)
                .activa(Boolean.TRUE)
                .build();

        PoliticaEntity politicaPremium = PoliticaEntity.builder()
                .nombre("Vacaciones premium")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(20)
                .antiguedadMinimaMeses(12)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(40)
                .activa(Boolean.TRUE)
                .build();

        politicaRepository.persist(politicaAnual);
        politicaRepository.persist(politicaPremium);

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(3001L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("15.0"))
                .diasUsados(new BigDecimal("0.0"))
                .diasAcumulados(new BigDecimal("0.0"))
                .build());

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(3002L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("10.0"))
                .diasUsados(new BigDecimal("5.0"))
                .diasAcumulados(new BigDecimal("1.0"))
                .build());

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(4001L)
                .politica(politicaPremium)
                .diasDisponibles(new BigDecimal("20.0"))
                .diasUsados(new BigDecimal("2.0"))
                .diasAcumulados(new BigDecimal("3.0"))
                .build());

        List<SaldoDiasEntity> saldosPoliticaAnual = saldoDiasRepository.findByPoliticaId(politicaAnual.getId());

        assertEquals(2, saldosPoliticaAnual.size());
        assertTrue(saldosPoliticaAnual.stream()
                .allMatch(saldo -> saldo.getPolitica().getId().equals(politicaAnual.getId())));

        List<SaldoDiasEntity> saldosPoliticaInexistente = saldoDiasRepository.findByPoliticaId(9999L);

        assertNotNull(saldosPoliticaInexistente);
        assertTrue(saldosPoliticaInexistente.isEmpty());
    }
}       assertNull(missing);
    }

    @Test
    @TestTransaction
    void shouldFindAllSaldoDiasByPoliticaIdAndReturnEmptyListWhenNoAssignmentsExist() {
        PoliticaEntity politicaAnual = PoliticaEntity.builder()
                .nombre("Vacaciones anuales")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(15)
                .antiguedadMinimaMeses(0)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(30)
                .activa(Boolean.TRUE)
                .build();

        PoliticaEntity politicaPremium = PoliticaEntity.builder()
                .nombre("Vacaciones premium")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(20)
                .antiguedadMinimaMeses(12)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(40)
                .activa(Boolean.TRUE)
                .build();

        politicaRepository.persist(politicaAnual);
        politicaRepository.persist(politicaPremium);

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(3001L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("15.0"))
                .diasUsados(new BigDecimal("0.0"))
                .diasAcumulados(new BigDecimal("0.0"))
                .build());

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(3002L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("10.0"))
                .diasUsados(new BigDecimal("5.0"))
                .diasAcumulados(new BigDecimal("1.0"))
                .build());

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(4001L)
                .politica(politicaPremium)
                .diasDisponibles(new BigDecimal("20.0"))
                .diasUsados(new BigDecimal("2.0"))
                .diasAcumulados(new BigDecimal("3.0"))
                .build());

        List<SaldoDiasEntity> saldosPoliticaAnual = saldoDiasRepository.findByPoliticaId(politicaAnual.getId());

        assertEquals(2, saldosPoliticaAnual.size());
        assertTrue(saldosPoliticaAnual.stream()
                .allMatch(saldo -> saldo.getPolitica().getId().equals(politicaAnual.getId())));

        List<SaldoDiasEntity> saldosPoliticaInexistente = saldoDiasRepository.findByPoliticaId(9999L);

        assertNotNull(saldosPoliticaInexistente);
        assertTrue(saldosPoliticaInexistente.isEmpty());
    }
}       assertNull(missing);
    }

    @Test
    @TestTransaction
    void shouldFindAllSaldoDiasByPoliticaIdAndReturnEmptyListWhenNoAssignmentsExist() {
        PoliticaEntity politicaAnual = PoliticaEntity.builder()
                .nombre("Vacaciones anuales")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(15)
                .antiguedadMinimaMeses(0)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(30)
                .activa(Boolean.TRUE)
                .build();

        PoliticaEntity politicaPremium = PoliticaEntity.builder()
                .nombre("Vacaciones premium")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(20)
                .antiguedadMinimaMeses(12)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(40)
                .activa(Boolean.TRUE)
                .build();

        politicaRepository.persist(politicaAnual);
        politicaRepository.persist(politicaPremium);

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(3001L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("15.0"))
                .diasUsados(new BigDecimal("0.0"))
                .diasAcumulados(new BigDecimal("0.0"))
                .build());

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(3002L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("10.0"))
                .diasUsados(new BigDecimal("5.0"))
                .diasAcumulados(new BigDecimal("1.0"))
                .build());

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(4001L)
                .politica(politicaPremium)
                .diasDisponibles(new BigDecimal("20.0"))
                .diasUsados(new BigDecimal("2.0"))
                .diasAcumulados(new BigDecimal("3.0"))
                .build());

        List<SaldoDiasEntity> saldosPoliticaAnual = saldoDiasRepository.findByPoliticaId(politicaAnual.getId());

        assertEquals(2, saldosPoliticaAnual.size());
        assertTrue(saldosPoliticaAnual.stream()
                .allMatch(saldo -> saldo.getPolitica().getId().equals(politicaAnual.getId())));

        List<SaldoDiasEntity> saldosPoliticaInexistente = saldoDiasRepository.findByPoliticaId(9999L);

        assertNotNull(saldosPoliticaInexistente);
        assertTrue(saldosPoliticaInexistente.isEmpty());
    }
}       assertNull(missing);
    }

    @Test
    @TestTransaction
    void shouldFindAllSaldoDiasByPoliticaIdAndReturnEmptyListWhenNoAssignmentsExist() {
        PoliticaEntity politicaAnual = PoliticaEntity.builder()
                .nombre("Vacaciones anuales")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(15)
                .antiguedadMinimaMeses(0)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(30)
                .activa(Boolean.TRUE)
                .build();

        PoliticaEntity politicaPremium = PoliticaEntity.builder()
                .nombre("Vacaciones premium")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(20)
                .antiguedadMinimaMeses(12)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(40)
                .activa(Boolean.TRUE)
                .build();

        politicaRepository.persist(politicaAnual);
        politicaRepository.persist(politicaPremium);

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(3001L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("15.0"))
                .diasUsados(new BigDecimal("0.0"))
                .diasAcumulados(new BigDecimal("0.0"))
                .build());

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(3002L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("10.0"))
                .diasUsados(new BigDecimal("5.0"))
                .diasAcumulados(new BigDecimal("1.0"))
                .build());

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(4001L)
                .politica(politicaPremium)
                .diasDisponibles(new BigDecimal("20.0"))
                .diasUsados(new BigDecimal("2.0"))
                .diasAcumulados(new BigDecimal("3.0"))
                .build());

        List<SaldoDiasEntity> saldosPoliticaAnual = saldoDiasRepository.findByPoliticaId(politicaAnual.getId());

        assertEquals(2, saldosPoliticaAnual.size());
        assertTrue(saldosPoliticaAnual.stream()
                .allMatch(saldo -> saldo.getPolitica().getId().equals(politicaAnual.getId())));

        List<SaldoDiasEntity> saldosPoliticaInexistente = saldoDiasRepository.findByPoliticaId(9999L);

        assertNotNull(saldosPoliticaInexistente);
        assertTrue(saldosPoliticaInexistente.isEmpty());
    }
}       assertNull(missing);
    }

    @Test
    @TestTransaction
    void shouldFindAllSaldoDiasByPoliticaIdAndReturnEmptyListWhenNoAssignmentsExist() {
        PoliticaEntity politicaAnual = PoliticaEntity.builder()
                .nombre("Vacaciones anuales")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(15)
                .antiguedadMinimaMeses(0)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(30)
                .activa(Boolean.TRUE)
                .build();

        PoliticaEntity politicaPremium = PoliticaEntity.builder()
                .nombre("Vacaciones premium")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(20)
                .antiguedadMinimaMeses(12)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(40)
                .activa(Boolean.TRUE)
                .build();

        politicaRepository.persist(politicaAnual);
        politicaRepository.persist(politicaPremium);

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(3001L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("15.0"))
                .diasUsados(new BigDecimal("0.0"))
                .diasAcumulados(new BigDecimal("0.0"))
                .build());

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(3002L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("10.0"))
                .diasUsados(new BigDecimal("5.0"))
                .diasAcumulados(new BigDecimal("1.0"))
                .build());

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(4001L)
                .politica(politicaPremium)
                .diasDisponibles(new BigDecimal("20.0"))
                .diasUsados(new BigDecimal("2.0"))
                .diasAcumulados(new BigDecimal("3.0"))
                .build());

        List<SaldoDiasEntity> saldosPoliticaAnual = saldoDiasRepository.findByPoliticaId(politicaAnual.getId());

        assertEquals(2, saldosPoliticaAnual.size());
        assertTrue(saldosPoliticaAnual.stream()
                .allMatch(saldo -> saldo.getPolitica().getId().equals(politicaAnual.getId())));

        List<SaldoDiasEntity> saldosPoliticaInexistente = saldoDiasRepository.findByPoliticaId(9999L);

        assertNotNull(saldosPoliticaInexistente);
        assertTrue(saldosPoliticaInexistente.isEmpty());
    }
}       assertNull(missing);
    }

    @Test
    @TestTransaction
    void shouldFindAllSaldoDiasByPoliticaIdAndReturnEmptyListWhenNoAssignmentsExist() {
        PoliticaEntity politicaAnual = PoliticaEntity.builder()
                .nombre("Vacaciones anuales")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(15)
                .antiguedadMinimaMeses(0)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(30)
                .activa(Boolean.TRUE)
                .build();

        PoliticaEntity politicaPremium = PoliticaEntity.builder()
                .nombre("Vacaciones premium")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(20)
                .antiguedadMinimaMeses(12)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(40)
                .activa(Boolean.TRUE)
                .build();

        politicaRepository.persist(politicaAnual);
        politicaRepository.persist(politicaPremium);

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(3001L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("15.0"))
                .diasUsados(new BigDecimal("0.0"))
                .diasAcumulados(new BigDecimal("0.0"))
                .build());

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(3002L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("10.0"))
                .diasUsados(new BigDecimal("5.0"))
                .diasAcumulados(new BigDecimal("1.0"))
                .build());

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(4001L)
                .politica(politicaPremium)
                .diasDisponibles(new BigDecimal("20.0"))
                .diasUsados(new BigDecimal("2.0"))
                .diasAcumulados(new BigDecimal("3.0"))
                .build());

        List<SaldoDiasEntity> saldosPoliticaAnual = saldoDiasRepository.findByPoliticaId(politicaAnual.getId());

        assertEquals(2, saldosPoliticaAnual.size());
        assertTrue(saldosPoliticaAnual.stream()
                .allMatch(saldo -> saldo.getPolitica().getId().equals(politicaAnual.getId())));

        List<SaldoDiasEntity> saldosPoliticaInexistente = saldoDiasRepository.findByPoliticaId(9999L);

        assertNotNull(saldosPoliticaInexistente);
        assertTrue(saldosPoliticaInexistente.isEmpty());
    }
}       assertNull(missing);
    }

    @Test
    @TestTransaction
    void shouldFindAllSaldoDiasByPoliticaIdAndReturnEmptyListWhenNoAssignmentsExist() {
        PoliticaEntity politicaAnual = PoliticaEntity.builder()
                .nombre("Vacaciones anuales")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(15)
                .antiguedadMinimaMeses(0)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(30)
                .activa(Boolean.TRUE)
                .build();

        PoliticaEntity politicaPremium = PoliticaEntity.builder()
                .nombre("Vacaciones premium")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(20)
                .antiguedadMinimaMeses(12)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(40)
                .activa(Boolean.TRUE)
                .build();

        politicaRepository.persist(politicaAnual);
        politicaRepository.persist(politicaPremium);

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(3001L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("15.0"))
                .diasUsados(new BigDecimal("0.0"))
                .diasAcumulados(new BigDecimal("0.0"))
                .build());

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(3002L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("10.0"))
                .diasUsados(new BigDecimal("5.0"))
                .diasAcumulados(new BigDecimal("1.0"))
                .build());

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(4001L)
                .politica(politicaPremium)
                .diasDisponibles(new BigDecimal("20.0"))
                .diasUsados(new BigDecimal("2.0"))
                .diasAcumulados(new BigDecimal("3.0"))
                .build());

        List<SaldoDiasEntity> saldosPoliticaAnual = saldoDiasRepository.findByPoliticaId(politicaAnual.getId());

        assertEquals(2, saldosPoliticaAnual.size());
        assertTrue(saldosPoliticaAnual.stream()
                .allMatch(saldo -> saldo.getPolitica().getId().equals(politicaAnual.getId())));

        List<SaldoDiasEntity> saldosPoliticaInexistente = saldoDiasRepository.findByPoliticaId(9999L);

        assertNotNull(saldosPoliticaInexistente);
        assertTrue(saldosPoliticaInexistente.isEmpty());
    }
}