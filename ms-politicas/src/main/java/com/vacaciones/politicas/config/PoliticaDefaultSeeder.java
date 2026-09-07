package com.vacaciones.politicas.config;

import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.repository.PoliticaRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PoliticaDefaultSeeder {

    private static final Logger LOG = Logger.getLogger(PoliticaDefaultSeeder.class);

    static final String NOMBRE_POLITICA_DEFECTO = "Politica estandar";
    static final int DIAS_BASE_ANIO_DEFECTO = 30;

    private final PoliticaRepository politicaRepository;

    public PoliticaDefaultSeeder(PoliticaRepository politicaRepository) {
        this.politicaRepository = politicaRepository;
    }

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (politicaRepository.findByEsPorDefectoTrue() != null) {
            LOG.info("Politica por defecto ya existe, se omite el seed");
            return;
        }

        LOG.infof("Creando politica por defecto con %d dias base al anio", DIAS_BASE_ANIO_DEFECTO);

        PoliticaEntity politicaPorDefecto = PoliticaEntity.builder()
                .nombre(NOMBRE_POLITICA_DEFECTO)
                .tipoVacacion("ANUAL")
                .diasBaseAnio(DIAS_BASE_ANIO_DEFECTO)
                .antiguedadMinimaMeses(0)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(DIAS_BASE_ANIO_DEFECTO)
                .activa(Boolean.TRUE)
                .esPorDefecto(Boolean.TRUE)
                .build();

        politicaRepository.persist(politicaPorDefecto);
    }
}
