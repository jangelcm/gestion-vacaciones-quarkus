package com.vacaciones.politicas.scheduler;

import static org.mockito.Mockito.verify;

import com.vacaciones.politicas.service.SaldoDiasService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RenovacionSaldoSchedulerTest {

    @Mock
    SaldoDiasService saldoDiasService;

    @InjectMocks
    RenovacionSaldoScheduler scheduler;

    @Test
    void shouldTriggerDailyBatchOnScheduleTick() {
        scheduler.renovarSaldosDelDia();
        verify(saldoDiasService).renovarSaldosAcumulablesDelDia(LocalDate.now());
    }
}
