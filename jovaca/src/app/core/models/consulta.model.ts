export type EstadoSolicitudConsulta = 'PENDIENTE' | 'APROBADA' | 'RECHAZADA' | 'CANCELADA';

export interface SolicitudConsultaDto {
    id: number;
    colaboradorId: string;
    fechaInicio: string;
    fechaFin: string;
    fechaSolicitud: string;
    estado: EstadoSolicitudConsulta;
    ultimaActualizacion: string;
}

export interface SolicitudHistorialDto {
    solicitudId: number;
    estado: EstadoSolicitudConsulta;
    detalle: string;
    fechaEvento: string;
}

export interface BalanceVacacionalDto {
    colaboradorId: number;
    politicaId: number;
    fechaInicioPolitica: string | null;
    diasDisponibles: number;
    diasGozados: number;
    diasHabilitados: number;
    saldoActual: number;
    diasTruncos: number;
    diasTrabajados: number;
    diasPendientes: number;
    diasAcumulados: number;
    fechaIngresoColaborador: string | null;
    motivoActualizacion: string;
    ultimaActualizacion: string;
}
