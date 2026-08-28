package com.vacaciones.politicas.testsupport;

import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.repository.PoliticaRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;

@ApplicationScoped
public class SaldoTestDataHelper {

    private final PoliticaRepository politicaRepository;
    private final SaldoDiasRepository saldoDiasRepository;

    public SaldoTestDataHelper(PoliticaRepository politicaRepository, SaldoDiasRepository saldoDiasRepository) {
        this.politicaRepository = politicaRepository;
        this.saldoDiasRepository = saldoDiasRepository;
    }

    @Transactional
    public void seedSaldo(Long colaboradorId, String diasDisponibles, String diasUsados) {
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

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(colaboradorId)
                .politica(politica)
                .diasDisponibles(new BigDecimal(diasDisponibles))
                .diasUsados(new BigDecimal(diasUsados))
                .diasAcumulados(BigDecimal.ZERO.setScale(1))
                .version(0)
                .build());
    }
}
