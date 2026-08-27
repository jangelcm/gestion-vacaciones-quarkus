export interface Solicitud {
    id: number;
    colaboradorId: number;
    fechaInicio: string;
    fechaFin: string;
    fechaSolicitud?: string;
    estado: 'PENDIENTE' | 'APROBADA' | 'RECHAZADA';
}

export type SolicitudPayload = Omit<Solicitud, 'id' | 'estado'>;
