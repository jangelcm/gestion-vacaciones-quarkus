package com.vacaciones.politicas.scheduler;

import com.vacaciones.politicas.service.SaldoDiasService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import org.jboss.logging.Logger;

@ApplicationScoped
public class RenovacionSaldoScheduler {

    private static final Logger LOG = Logger.getLogger(RenovacionSaldoScheduler.class);

    private final SaldoDiasService saldoDiasService;

    public RenovacionSaldoScheduler(SaldoDiasService saldoDiasService) {
        this.saldoDiasService = saldoDiasService;
    }

    @Scheduled(cron = "{renovacion.saldo.cron:0 0 1 * * ?}")
    void renovarSaldosDelDia() {
        LOG.info("Ejecutando renovacion diaria de saldos acumulables");
        saldoDiasService.renovarSaldosAcumulablesDelDia(LocalDate.now());
    }
}
