package com.vacaciones.politicas.service;

import com.vacaciones.politicas.dto.response.SaldoDiasResponseDto;
import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.exception.BadRequestException;
import com.vacaciones.politicas.exception.ResourceNotFoundException;
import com.vacaciones.politicas.exception.RuntimeCustomException;
import com.vacaciones.politicas.messaging.event.DiasDisponiblesActualizadosEvent;
import com.vacaciones.politicas.messaging.event.PoliticaActualizadaEvent;
import com.vacaciones.politicas.messaging.event.SolicitudAprobadaEvent;
import com.vacaciones.politicas.messaging.event.SolicitudCanceladaEvent;
import com.vacaciones.politicas.repository.PoliticaRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SaldoDiasService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    static final String MOTIVO_DESCUENTO_SOLICITUD_APROBADA = "DESCUENTO_SOLICITUD_APROBADA";
    static final String MOTIVO_DEVOLUCION_SOLICITUD_CANCELADA = "DEVOLUCION_SOLICITUD_CANCELADA";
    static final String MOTIVO_ASIGNACION_POLITICA = "ASIGNACION_POLITICA";
    static final String MOTIVO_RENOVACION_PERIODO = "RENOVACION_PERIODO_ACUMULACION";
    static final String MOTIVO_BATCH_DIARIO = "BATCH_DIARIO";

    static final BigDecimal DIAS_LABORALES_PERIODO = new BigDecimal("360");

    private static final Logger LOG = Logger.getLogger(SaldoDiasService.class);

    private final SaldoDiasRepository saldoDiasRepository;
    private final PoliticaRepository politicaRepository;
    private final SaldoDiasWriteOperations saldoDiasWriteOperations;
    private final Emitter<DiasDisponiblesActualizadosEvent> diasDisponiblesEmitter;
    private final Emitter<PoliticaActualizadaEvent> politicaActualizadaEmitter;

    public SaldoDiasService(
            SaldoDiasRepository saldoDiasRepository,
            PoliticaRepository politicaRepository,
            SaldoDiasWriteOperations saldoDiasWriteOperations,
            @Channel("dias-disponibles-actualizados-out") Emitter<DiasDisponiblesActualizadosEvent> diasDisponiblesEmitter,
            @Channel("politica-actualizada-out") Emitter<PoliticaActualizadaEvent> politicaActualizadaEmitter) {
        this.saldoDiasRepository = saldoDiasRepository;
        this.politicaRepository = politicaRepository;
        this.saldoDiasWriteOperations = saldoDiasWriteOperations;
        this.diasDisponiblesEmitter = diasDisponiblesEmitter;
        this.politicaActualizadaEmitter = politicaActualizadaEmitter;
    }

    @Transactional
    public void asignarPolitica(
            Long colaboradorId,
            Long politicaId,
            LocalDate fechaInicioPolitica,
            LocalDate fechaIngresoColaborador) {

        if (fechaIngresoColaborador == null) {
            throw new BadRequestException("La fecha de ingreso del colaborador es obligatoria");
        }

        SaldoDiasEntity existing = saldoDiasRepository.findByColaboradorId(colaboradorId);
        if (existing != null) {
            throw new RuntimeCustomException(
                    "El colaborador ya tiene una politica asignada",
                    Response.Status.CONFLICT);
        }

        PoliticaEntity politica = politicaRepository.findById(politicaId);
        if (politica == null) {
            throw new ResourceNotFoundException("Politica no encontrada");
        }

        if (!Boolean.TRUE.equals(politica.getActiva())) {
            throw new BadRequestException("La politica no esta activa");
        }

        LocalDate fechaInicio = fechaInicioPolitica != null ? fechaInicioPolitica : LocalDate.now();

        // 1. Días trabajados calculados según ingreso real
        long diasTranscurridos = ChronoUnit.DAYS.between(fechaIngresoColaborador, fechaInicio);
        int diasTrabajados = Math.max(0, (int) diasTranscurridos);

        // 2. Creación inicial de la entidad con valores a cero
        SaldoDiasEntity nuevoSaldo = SaldoDiasEntity.builder()
                .colaboradorId(colaboradorId)
                .politica(politica)
                .diasDisponibles(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .diasUsados(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .diasAcumulados(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .diasPendientes(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .diasTrabajados(diasTrabajados)
                .fechaIngresoColaborador(fechaIngresoColaborador)
                .fechaAsignacionPolitica(fechaInicio)
                .version(0)
                .build();

        saldoDiasRepository.persist(nuevoSaldo);
        saldoDiasRepository.getEntityManager().flush();

        // 3. Recalcular disponibilidad de saldo en función de la antigüedad
        recalcularSaldoDisponible(nuevoSaldo);
        publicarDiasActualizados(nuevoSaldo, MOTIVO_ASIGNACION_POLITICA);
        publicarPoliticaActualizadaPorAsignacion(nuevoSaldo);
    }

    @Transactional
    public void asignarPolitica(Long colaboradorId, Long politicaId, Integer antiguedadMeses) {
        if (antiguedadMeses == null) {
            throw new BadRequestException("Debe proporcionar la antigüedad en meses o la fecha de ingreso");
        }
        LocalDate fechaIngreso = LocalDate.now().minusMonths(antiguedadMeses);
        asignarPolitica(colaboradorId, politicaId, LocalDate.now(), fechaIngreso);
    }

    @Transactional
    public void asignarPoliticaPorDefectoSiNoTiene(Long colaboradorId, LocalDate fechaIngresoColaborador) {
        if (saldoDiasRepository.findByColaboradorId(colaboradorId) != null) {
            return;
        }

        PoliticaEntity politicaPorDefecto = politicaRepository.findByEsPorDefectoTrue();
        if (politicaPorDefecto == null) {
            throw new ResourceNotFoundException("No existe una politica por defecto configurada");
        }

        asignarPolitica(colaboradorId, politicaPorDefecto.getId(), LocalDate.now(), fechaIngresoColaborador);
    }

    @Transactional
    public void asignarPoliticaPorDefectoSiNoTiene(Long colaboradorId) {
        throw new BadRequestException("La fecha de ingreso del colaborador es obligatoria para asignar la política por defecto");
    }

    public void renovarSaldosAcumulablesDelDia(LocalDate fecha) {
        java.util.List<SaldoDiasEntity> saldos = saldoDiasRepository.findParaProcesoDiario();

        for (SaldoDiasEntity saldo : saldos) {
            try {
                int trabajadosAntes = safeDiasTrabajados(saldo);
                SaldoDiasEntity procesado = saldoDiasWriteOperations.ejecutarProcesoDiario(saldo);
                recalcularSaldoDisponible(procesado);
                boolean aniversario = (trabajadosAntes + 1) % 360 == 0;
                publicarDiasActualizados(procesado, aniversario ? MOTIVO_RENOVACION_PERIODO : MOTIVO_BATCH_DIARIO);
            } catch (RuntimeException e) {
                LOG.errorf(e, "Fallo al renovar el saldo del colaborador %d, se continua con el resto",
                        saldo.getColaboradorId());
            }
        }
    }

    public java.util.List<SaldoDiasResponseDto> getByPoliticaId(Long politicaId) {
        return saldoDiasRepository.findByPoliticaId(politicaId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    public SaldoDiasResponseDto getByColaboradorId(Long colaboradorId) {
        SaldoDiasEntity saldoDias = saldoDiasRepository.findByColaboradorId(colaboradorId);

        if (saldoDias == null) {
            throw new ResourceNotFoundException("Saldo no encontrado para el colaborador");
        }

        return toResponseDto(saldoDias);
    }

    public void procesarSolicitudAprobada(SolicitudAprobadaEvent evento) {
        descontarDias(
                evento.colaboradorId(),
                evento.solicitudId(),
                evento.diasAprobados(),
                SaldoDiasWriteOperations.ORIGEN_SOLICITUD_APROBADA,
                evento.eventoId());
    }

    public void procesarSolicitudCancelada(SolicitudCanceladaEvent evento) {
        devolverDias(
                evento.colaboradorId(),
                evento.solicitudId(),
                evento.diasADevolver(),
                SaldoDiasWriteOperations.ORIGEN_SOLICITUD_CANCELADA,
                evento.eventoId());
    }

    public void descontarDias(
            Long colaboradorId,
            Long solicitudId,
            BigDecimal dias,
            String eventoOrigen,
            String eventoId) {
        SaldoDiasEntity saldo = ejecutarConReintento(() -> saldoDiasWriteOperations.ejecutarDescuento(
                colaboradorId, solicitudId, dias, eventoOrigen, eventoId));
        if (saldo != null) {
            recalcularSaldoDisponible(saldo);
            publicarDiasActualizados(saldo, MOTIVO_DESCUENTO_SOLICITUD_APROBADA);
        }
    }

    public void devolverDias(
            Long colaboradorId,
            Long solicitudId,
            BigDecimal dias,
            String eventoOrigen,
            String eventoId) {
        SaldoDiasEntity saldo = ejecutarConReintento(() -> saldoDiasWriteOperations.ejecutarDevolucion(
                colaboradorId, solicitudId, dias, eventoOrigen, eventoId));
        if (saldo != null) {
            recalcularSaldoDisponible(saldo);
            publicarDiasActualizados(saldo, MOTIVO_DEVOLUCION_SOLICITUD_CANCELADA);
        }
    }

    private SaldoDiasEntity ejecutarConReintento(Supplier<SaldoDiasEntity> operacion) {
        try {
            return operacion.get();
        } catch (OptimisticLockException primeraExcepcion) {
            try {
                return operacion.get();
            } catch (OptimisticLockException segundaExcepcion) {
                throw segundaExcepcion;
            }
        }
    }

    private void publicarDiasActualizados(SaldoDiasEntity saldo, String motivo) {
        BigDecimal diasTruncos = calcularDiasTruncos(saldo);
        BigDecimal diasHabilitados = calcularDiasHabilitados(saldo);
        BigDecimal saldoActual = calcularSaldoActual(saldo, diasHabilitados);
        diasDisponiblesEmitter.send(new DiasDisponiblesActualizadosEvent(
                saldo.getColaboradorId(),
                saldo.getPolitica() != null ? saldo.getPolitica().getId() : null,
                saldo.getFechaAsignacionPolitica(),
                saldo.getDiasDisponibles(),
                saldo.getDiasUsados(),
                diasHabilitados,
                saldoActual,
                diasTruncos,
                safeDiasTrabajados(saldo),
                safeBigDecimal(saldo.getDiasPendientes()),
                motivo,
                LocalDateTime.now()));
    }

    private void publicarPoliticaActualizadaPorAsignacion(SaldoDiasEntity saldo) {
        PoliticaEntity politica = saldo.getPolitica();
        if (politica == null) {
            return;
        }
        politicaActualizadaEmitter.send(new PoliticaActualizadaEvent(
                politica.getId(),
                saldo.getColaboradorId(),
                saldo.getFechaAsignacionPolitica(),
                politica.getNombre(),
                politica.getTipoVacacion(),
                politica.getDiasBaseAnio(),
                politica.getActiva(),
                LocalDateTime.now()));
    }

    void recalcularSaldoDisponible(SaldoDiasEntity saldo) {
        BigDecimal diasHabilitados = calcularDiasHabilitados(saldo);
        BigDecimal saldoActual = calcularSaldoActual(saldo, diasHabilitados);
        saldo.setDiasDisponibles(saldoActual);
        saldoDiasRepository.persist(saldo);
        EntityManager entityManager = saldoDiasRepository.getEntityManager();
        if (entityManager != null) {
            entityManager.flush();
        }
    }

    BigDecimal calcularDiasTruncos(SaldoDiasEntity saldo) {
        if (saldo.getPolitica() == null || saldo.getPolitica().getDiasBaseAnio() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal diasBase = BigDecimal.valueOf(saldo.getPolitica().getDiasBaseAnio());
        int diasTrabajadosEnPeriodo = safeDiasTrabajados(saldo) % 360;

        return BigDecimal.valueOf(diasTrabajadosEnPeriodo)
                .multiply(diasBase)
                .divide(DIAS_LABORALES_PERIODO, 2, RoundingMode.HALF_UP);
    }

    BigDecimal calcularDiasHabilitados(SaldoDiasEntity saldo) {
        return safeBigDecimal(saldo.getDiasAcumulados());
    }

    BigDecimal calcularSaldoActual(SaldoDiasEntity saldo, BigDecimal diasHabilitados) {
        int antiguedadMeses = calcularAntiguedadMeses(saldo.getFechaIngresoColaborador());
        int antiguedadRequerida = saldo.getPolitica() != null ? saldo.getPolitica().getAntiguedadMinimaMeses() : 0;

        // Si no ha cumplido el periodo de gracia/antigüedad requerida, el saldo disponible es 0.
        if (antiguedadMeses < antiguedadRequerida) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal saldoCalculado = diasHabilitados
                .subtract(safeBigDecimal(saldo.getDiasUsados()))
                .subtract(safeBigDecimal(saldo.getDiasPendientes()));

        return saldoCalculado.max(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal safeBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private int safeDiasTrabajados(SaldoDiasEntity saldo) {
        return saldo.getDiasTrabajados() == null ? 0 : saldo.getDiasTrabajados();
    }

    private int calcularAntiguedadMeses(LocalDate fechaIngresoColaborador) {
        if (fechaIngresoColaborador == null) {
            return 0;
        }
        LocalDate hoy = LocalDate.now();
        if (fechaIngresoColaborador.isAfter(hoy)) {
            return 0;
        }
        return (int) ChronoUnit.MONTHS.between(
                fechaIngresoColaborador.withDayOfMonth(1),
                hoy.withDayOfMonth(1));
    }

    private String formatDate(LocalDate value) {
        return value == null ? null : value.toString();
    }

    private SaldoDiasResponseDto toResponseDto(SaldoDiasEntity saldoDias) {
        BigDecimal diasTruncos = calcularDiasTruncos(saldoDias);
        BigDecimal diasHabilitados = calcularDiasHabilitados(saldoDias);
        BigDecimal saldoActual = calcularSaldoActual(saldoDias, diasHabilitados);
        return new SaldoDiasResponseDto(
                saldoDias.getId(),
                saldoDias.getColaboradorId(),
                saldoDias.getPolitica() != null ? saldoDias.getPolitica().getId() : null,
                String.valueOf(saldoDias.getDiasDisponibles()),
                String.valueOf(saldoDias.getDiasUsados()),
                String.valueOf(saldoDias.getDiasAcumulados()),
                String.valueOf(diasHabilitados),
                String.valueOf(saldoActual),
                String.valueOf(diasTruncos),
                String.valueOf(safeDiasTrabajados(saldoDias)),
                String.valueOf(safeBigDecimal(saldoDias.getDiasPendientes())),
                formatDate(saldoDias.getFechaIngresoColaborador()),
                formatDate(saldoDias.getFechaAsignacionPolitica()),
                saldoDias.getCreatedAt() != null ? saldoDias.getCreatedAt().format(FORMATTER) : null,
                saldoDias.getUpdatedAt() != null ? saldoDias.getUpdatedAt().format(FORMATTER) : null);
    }
}