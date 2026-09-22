package com.vacaciones.politicas.service;

import com.vacaciones.politicas.dto.request.ValidarSolicitudRequestDto;
import com.vacaciones.politicas.dto.response.ValidarSolicitudResponseDto;
import com.vacaciones.politicas.entity.ReglaEspecialEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.repository.ReglaEspecialRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class ValidacionService {

    private static final String SALDO_INSUFICIENTE = "Saldo insuficiente para la solicitud";
    private static final String SALDO_NO_ENCONTRADO = "No se encontro saldo de dias para el colaborador";
    private static final int DIAS_MINIMOS_ANTICIPACION = 10;
    private static final String ANTICIPACION_INSUFICIENTE =
            "La solicitud debe realizarse con al menos 10 dias de anticipacion a la fecha de inicio";
    private static final int DIAS_MINIMOS_POR_SOLICITUD = 10;
    private static final String DIAS_MINIMOS_INSUFICIENTES =
            "La solicitud debe ser de al menos 10 dias calendario";

    private final SaldoDiasRepository saldoDiasRepository;
    private final ReglaEspecialRepository reglaEspecialRepository;

    public ValidacionService(SaldoDiasRepository saldoDiasRepository, ReglaEspecialRepository reglaEspecialRepository) {
        this.saldoDiasRepository = saldoDiasRepository;
        this.reglaEspecialRepository = reglaEspecialRepository;
    }

    public ValidarSolicitudResponseDto validarSolicitud(ValidarSolicitudRequestDto request, Integer antiguedadMeses) {
        // 1. Validar anticipación
        long diasAnticipacion = ChronoUnit.DAYS.between(LocalDate.now(), request.fechaInicio());
        if (diasAnticipacion < DIAS_MINIMOS_ANTICIPACION) {
            return new ValidarSolicitudResponseDto(false, 0, ANTICIPACION_INSUFICIENTE);
        }

        // 2. Calcular días calendario continuos solicitados (Base de cálculo legal)
        long diasCalendario = ChronoUnit.DAYS.between(request.fechaInicio(), request.fechaFin()) + 1;
        if (diasCalendario < DIAS_MINIMOS_POR_SOLICITUD) {
            return new ValidarSolicitudResponseDto(false, diasCalendario, DIAS_MINIMOS_INSUFICIENTES);
        }

        SaldoDiasEntity saldoDias = saldoDiasRepository.findByColaboradorId(request.colaboradorId());
        if (saldoDias == null) {
            return new ValidarSolicitudResponseDto(false, diasCalendario, SALDO_NO_ENCONTRADO);
        }

        Integer antiguedadCalculada = calcularAntiguedadMeses(saldoDias.getFechaIngresoColaborador(), antiguedadMeses);
        long diasAdicionales = calcularDiasAdicionales(saldoDias, antiguedadCalculada);

        long diasSolicitados = diasCalendario;

        BigDecimal saldoEfectivo = saldoDias.getDiasDisponibles().add(BigDecimal.valueOf(diasAdicionales));
        if (saldoEfectivo.compareTo(BigDecimal.valueOf(diasSolicitados)) < 0) {
            return new ValidarSolicitudResponseDto(false, diasSolicitados, SALDO_INSUFICIENTE);
        }

        return new ValidarSolicitudResponseDto(true, diasSolicitados, null);
    }

    private long calcularDiasAdicionales(SaldoDiasEntity saldoDias, Integer antiguedadMeses) {
        if (saldoDias.getPolitica() == null) {
            return 0;
        }

        List<ReglaEspecialEntity> reglasEspeciales = reglaEspecialRepository.listAll();
        return reglasEspeciales.stream()
                .filter(ReglaEspecialEntity::getActiva)
                .filter(regla -> regla.getPolitica() != null
                        && regla.getPolitica().getId() != null
                        && regla.getPolitica().getId().equals(saldoDias.getPolitica().getId()))
                .filter(regla -> cumpleCondicionAntiguedad(regla.getCondicion(), antiguedadMeses))
                .mapToLong(ReglaEspecialEntity::getDiasAdicionales)
                .sum();
    }

    private boolean cumpleCondicionAntiguedad(String condicion, Integer antiguedadMeses) {
        if (condicion == null || antiguedadMeses == null) {
            return false;
        }
        if (!condicion.startsWith("ANTIGUEDAD>=")) {
            return false;
        }

        int mesesRequeridos = Integer.parseInt(condicion.substring("ANTIGUEDAD>=".length()));
        return antiguedadMeses >= mesesRequeridos;
    }

    private Integer calcularAntiguedadMeses(LocalDate fechaIngreso, Integer antiguedadFallback) {
        if (fechaIngreso == null) {
            return antiguedadFallback;
        }
        LocalDate hoy = LocalDate.now();
        if (fechaIngreso.isAfter(hoy)) {
            return 0;
        }
        return (int) ChronoUnit.MONTHS.between(fechaIngreso.withDayOfMonth(1), hoy.withDayOfMonth(1));
    }
}