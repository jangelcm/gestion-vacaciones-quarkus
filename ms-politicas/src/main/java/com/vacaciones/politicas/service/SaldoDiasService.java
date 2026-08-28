package com.vacaciones.politicas.service;

import com.vacaciones.politicas.dto.response.SaldoDiasResponseDto;
import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.exception.BadRequestException;
import com.vacaciones.politicas.exception.ResourceNotFoundException;
import com.vacaciones.politicas.exception.RuntimeCustomException;
import com.vacaciones.politicas.messaging.event.DiasDisponiblesActualizadosEvent;
import com.vacaciones.politicas.messaging.event.SolicitudAprobadaEvent;
import com.vacaciones.politicas.messaging.event.SolicitudCanceladaEvent;
import com.vacaciones.politicas.repository.PoliticaRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class SaldoDiasService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    static final String MOTIVO_DESCUENTO_SOLICITUD_APROBADA = "DESCUENTO_SOLICITUD_APROBADA";
    static final String MOTIVO_DEVOLUCION_SOLICITUD_CANCELADA = "DEVOLUCION_SOLICITUD_CANCELADA";
    static final String MOTIVO_ASIGNACION_POLITICA = "ASIGNACION_POLITICA";

    private final SaldoDiasRepository saldoDiasRepository;
    private final PoliticaRepository politicaRepository;
    private final SaldoDiasWriteOperations saldoDiasWriteOperations;
    private final Emitter<DiasDisponiblesActualizadosEvent> diasDisponiblesEmitter;

    public SaldoDiasService(
            SaldoDiasRepository saldoDiasRepository,
            PoliticaRepository politicaRepository,
            SaldoDiasWriteOperations saldoDiasWriteOperations,
            @Channel("dias-disponibles-actualizados-out") Emitter<DiasDisponiblesActualizadosEvent> diasDisponiblesEmitter) {
        this.saldoDiasRepository = saldoDiasRepository;
        this.politicaRepository = politicaRepository;
        this.saldoDiasWriteOperations = saldoDiasWriteOperations;
        this.diasDisponiblesEmitter = diasDisponiblesEmitter;
    }

    @Transactional
    public void asignarPolitica(Long colaboradorId, Long politicaId) {
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

        SaldoDiasEntity nuevoSaldo = SaldoDiasEntity.builder()
                .colaboradorId(colaboradorId)
                .politica(politica)
                .diasDisponibles(BigDecimal.valueOf(politica.getDiasBaseAnio()).setScale(1))
                .diasUsados(BigDecimal.ZERO.setScale(1))
                .diasAcumulados(BigDecimal.ZERO.setScale(1))
                .version(0)
                .build();
        saldoDiasRepository.persist(nuevoSaldo);
        saldoDiasRepository.getEntityManager().flush();
        publicarDiasActualizados(nuevoSaldo, MOTIVO_ASIGNACION_POLITICA);
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
        diasDisponiblesEmitter.send(new DiasDisponiblesActualizadosEvent(
                saldo.getColaboradorId(),
                saldo.getDiasDisponibles(),
                saldo.getDiasUsados(),
                motivo,
                LocalDateTime.now()));
    }

    private SaldoDiasResponseDto toResponseDto(SaldoDiasEntity saldoDias) {
        return new SaldoDiasResponseDto(
                saldoDias.getId(),
                saldoDias.getColaboradorId(),
                saldoDias.getPolitica() != null ? saldoDias.getPolitica().getId() : null,
                String.valueOf(saldoDias.getDiasDisponibles()),
                String.valueOf(saldoDias.getDiasUsados()),
                String.valueOf(saldoDias.getDiasAcumulados()),
                saldoDias.getCreatedAt() != null ? saldoDias.getCreatedAt().format(FORMATTER) : null,
                saldoDias.getUpdatedAt() != null ? saldoDias.getUpdatedAt().format(FORMATTER) : null);
    }
}
