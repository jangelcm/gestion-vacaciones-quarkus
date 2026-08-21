package com.vacaciones.politicas.config;

import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.entity.ReglaEspecialEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.repository.PoliticaRepository;
import com.vacaciones.politicas.repository.ReglaEspecialRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import org.jboss.logging.Logger;

@ApplicationScoped
@IfBuildProfile("dev")
public class DataSeeder {

    private static final Logger LOG = Logger.getLogger(DataSeeder.class);

    private final PoliticaRepository politicaRepository;
    private final ReglaEspecialRepository reglaEspecialRepository;
    private final SaldoDiasRepository saldoDiasRepository;

    public DataSeeder(
            PoliticaRepository politicaRepository,
            ReglaEspecialRepository reglaEspecialRepository,
            SaldoDiasRepository saldoDiasRepository) {
        this.politicaRepository = politicaRepository;
        this.reglaEspecialRepository = reglaEspecialRepository;
        this.saldoDiasRepository = saldoDiasRepository;
    }

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (politicaRepository.count() > 0) {
            LOG.info("Datos de prueba ya existen, se omite el seed");
            return;
        }

        LOG.info("Cargando datos de prueba...");

        PoliticaEntity politicaAnual = PoliticaEntity.builder()
                .nombre("Vacaciones anuales")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(15)
                .antiguedadMinimaMeses(0)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(30)
                .activa(Boolean.TRUE)
                .build();
        politicaRepository.persist(politicaAnual);

        PoliticaEntity politicaPremium = PoliticaEntity.builder()
                .nombre("Vacaciones premium")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(20)
                .antiguedadMinimaMeses(12)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(40)
                .activa(Boolean.TRUE)
                .build();
        politicaRepository.persist(politicaPremium);

        reglaEspecialRepository.persist(ReglaEspecialEntity.builder()
                .politica(politicaAnual)
                .condicion("ANTIGUEDAD>=60")
                .diasAdicionales(3)
                .descripcion("3 dias adicionales por antiguedad >= 60 meses")
                .activa(Boolean.TRUE)
                .build());

        // 1001: saldo suficiente -> validacion aprobada
        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(1001L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("10.0"))
                .diasUsados(new BigDecimal("2.0"))
                .diasAcumulados(new BigDecimal("1.0"))
                .version(0)
                .build());

        // 1002: saldo insuficiente -> validacion rechazada
        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(1002L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("2.0"))
                .diasUsados(new BigDecimal("8.0"))
                .diasAcumulados(new BigDecimal("0.0"))
                .version(0)
                .build());

        // 1003: 5 dias + regla especial (antiguedad 60) -> puede aprobar 7 dias habiles
        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(1003L)
                .politica(politicaAnual)
                .diasDisponibles(new BigDecimal("5.0"))
                .diasUsados(new BigDecimal("0.0"))
                .diasAcumulados(new BigDecimal("0.0"))
                .version(0)
                .build());

        // 1004: sujeto a antiguedad minima de politica premium (12 meses)
        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(1004L)
                .politica(politicaPremium)
                .diasDisponibles(new BigDecimal("15.0"))
                .diasUsados(new BigDecimal("0.0"))
                .diasAcumulados(new BigDecimal("0.0"))
                .version(0)
                .build());

        LOG.info("Datos de prueba cargados: politicas=2, colaboradores=1001,1002,1003,1004");
    }
}
