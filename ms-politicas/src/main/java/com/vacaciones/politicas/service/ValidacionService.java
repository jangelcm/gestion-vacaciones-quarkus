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

    public long calcularDiasHabiles(LocalDate fechaInicio, LocalDate fechaFin) {
        long diasHabiles = 0;
        LocalDate fecha = fechaInicio;

        while (!fecha.isAfter(fechaFin)) {
            if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY && fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {
                diasHabiles++;
            }
            fecha = fecha.plusDays(1);
        }

        return diasHabiles;
    }

    public ValidarSolicitudResponseDto validarSolicitud(ValidarSolicitudRequestDto request, Integer antiguedadMeses) {
        long diasHabiles = calcularDiasHabiles(request.fechaInicio(), request.fechaFin());

        long diasAnticipacion = ChronoUnit.DAYS.between(LocalDate.now(), request.fechaInicio());
        if (diasAnticipacion < DIAS_MINIMOS_ANTICIPACION) {
            return new ValidarSolicitudResponseDto(false, diasHabiles, ANTICIPACION_INSUFICIENTE);
        }

        long diasCalendario = ChronoUnit.DAYS.between(request.fechaInicio(), request.fechaFin()) + 1;
        if (diasCalendario < DIAS_MINIMOS_POR_SOLICITUD) {
            return new ValidarSolicitudResponseDto(false, diasHabiles, DIAS_MINIMOS_INSUFICIENTES);
        }

        SaldoDiasEntity saldoDias = saldoDiasRepository.findByColaboradorId(request.colaboradorId());
        if (saldoDias == null) {
            return new ValidarSolicitudResponseDto(false, diasHabiles, SALDO_NO_ENCONTRADO);
        }

        long diasAdicionales = calcularDiasAdicionales(saldoDias, antiguedadMeses);
        long diasSolicitados = diasHabiles;

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
}