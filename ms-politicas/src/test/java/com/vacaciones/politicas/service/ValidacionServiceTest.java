package com.vacaciones.politicas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.vacaciones.politicas.dto.request.ValidarSolicitudRequestDto;
import com.vacaciones.politicas.dto.response.ValidarSolicitudResponseDto;
import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.entity.ReglaEspecialEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.repository.ReglaEspecialRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidacionServiceTest {

    @Mock
    SaldoDiasRepository saldoDiasRepository;

    @Mock
    ReglaEspecialRepository reglaEspecialRepository;

    @InjectMocks
    ValidacionService validacionService;

    @Test
    void shouldCalculateBusinessDaysExcludingWeekends() {
        long diasHabiles = validacionService.calcularDiasHabiles(
                LocalDate.of(2026, 8, 14),
                LocalDate.of(2026, 8, 18));

        assertEquals(3, diasHabiles);
    }

    @Test
    void shouldRejectWhenAvailableBalanceIsLowerThanRequestedDays() {
        PoliticaEntity politica = buildPolitica();
        SaldoDiasEntity saldo = buildSaldoDias(1001L, politica, "2.0");
        ValidarSolicitudRequestDto request = new ValidarSolicitudRequestDto(
                1001L,
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 8, 21),
                "ANUAL",
                12);

        when(saldoDiasRepository.findByColaboradorId(1001L)).thenReturn(saldo);
        when(reglaEspecialRepository.listAll()).thenReturn(List.of());

        ValidarSolicitudResponseDto response = validacionService.validarSolicitud(request, 12);

        assertFalse(response.aprobado());
        assertEquals(5, response.diasSolicitados());
        assertEquals("Saldo insuficiente para la solicitud", response.motivoRechazo());
    }

    @Test
    void shouldApproveWhenAvailableBalanceIsSufficient() {
        PoliticaEntity politica = buildPolitica();
        SaldoDiasEntity saldo = buildSaldoDias(1002L, politica, "10.0");
        ValidarSolicitudRequestDto request = new ValidarSolicitudRequestDto(
                1002L,
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 8, 21),
                "ANUAL",
                12);

        when(saldoDiasRepository.findByColaboradorId(1002L)).thenReturn(saldo);
        when(reglaEspecialRepository.listAll()).thenReturn(List.of());

        ValidarSolicitudResponseDto response = validacionService.validarSolicitud(request, 12);

        assertTrue(response.aprobado());
        assertEquals(5, response.diasSolicitados());
        assertNull(response.motivoRechazo());
    }

    @Test
    void shouldApplySpecialRuleWhenSeniorityConditionMatches() {
        PoliticaEntity politica = buildPolitica();
        SaldoDiasEntity saldo = buildSaldoDias(1003L, politica, "5.0");
        ReglaEspecialEntity reglaEspecial = ReglaEspecialEntity.builder()
                .id(1L)
                .politica(politica)
                .condicion("ANTIGUEDAD>=60")
                .diasAdicionales(3)
                .descripcion("Otorga 3 dias adicionales por antiguedad")
                .activa(Boolean.TRUE)
                .build();
        ValidarSolicitudRequestDto request = new ValidarSolicitudRequestDto(
                1003L,
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 8, 25),
                "ANUAL",
                60);

        when(saldoDiasRepository.findByColaboradorId(1003L)).thenReturn(saldo);
        when(reglaEspecialRepository.listAll()).thenReturn(List.of(reglaEspecial));

        ValidarSolicitudResponseDto response = validacionService.validarSolicitud(request, 60);

        assertTrue(response.aprobado());
        assertEquals(7, response.diasSolicitados());
        assertNull(response.motivoRechazo());
    }

    private PoliticaEntity buildPolitica() {
        return PoliticaEntity.builder()
                .id(1L)
                .nombre("Vacaciones anuales")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(15)
                .antiguedadMinimaMeses(0)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(30)
                .activa(Boolean.TRUE)
                .build();
    }

    private SaldoDiasEntity buildSaldoDias(Long colaboradorId, PoliticaEntity politica, String diasDisponibles) {
        return SaldoDiasEntity.builder()
                .id(1L)
                .colaboradorId(colaboradorId)
                .politica(politica)
                .diasDisponibles(new BigDecimal(diasDisponibles))
                .diasUsados(new BigDecimal("0.0"))
                .diasAcumulados(new BigDecimal("0.0"))
                .version(0)
                .build();
    }
}