export interface Aprobacion {
    id: number;
    solicitudId: number;
    colaboradorId: string | null;
    fechaInicio: string | null;
    fechaFin: string | null;
    aprobadorId: string;
    estado: 'PENDIENTE' | 'APROBADO' | 'RECHAZADO';
    comentario: string | null;
    fechaAprobacion: string | null;
    nivelAprobacion: number;
}

export interface AprobarPayload {
    aprobadorId: string;
    comentario: string;
}

export interface RechazarPayload {
    aprobadorId: string;
    motivo: string;
}
