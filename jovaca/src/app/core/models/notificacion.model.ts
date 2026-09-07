export interface Notificacion {
    id: string;
    eventoId: string | null;
    tipo: 'EMAIL' | 'WEBSOCKET' | 'RECORDATORIO';
    destinatario: {
        colaboradorId: number;
        email: string;
        nombre: string;
    };
    asunto: string;
    cuerpo: string;
    estado: 'PENDIENTE' | 'ENVIADO' | 'FALLIDO';
    eventoOrigen: string | null;
    fechaCreacion: string;
    fechaEnvio: string | null;
}
