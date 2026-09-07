export interface Aprobacion {
    id: number;
    solicitudId: number;
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
