import { HttpErrorResponse } from '@angular/common/http';

/**
 * Todos los backends devuelven {hora, mensaje, url, codeStatus} ante cualquier excepcion no
 * controlada. Esta funcion extrae ese mensaje real para mostrarlo en vez de un texto generico
 * fijo que oculta la causa (ej. "saldo no encontrado" vs. un "no se pudo cargar" genérico).
 */
export function extraerMensajeError(err: unknown, fallback: string): string {
    if (err instanceof HttpErrorResponse) {
        const body = err.error;
        if (typeof body === 'string' && body.trim()) {
            return body;
        }
        if (body && typeof body === 'object') {
            const mensaje = (body as Record<string, unknown>)['mensaje'] ?? (body as Record<string, unknown>)['message'];
            if (typeof mensaje === 'string' && mensaje.trim()) {
                return mensaje;
            }
        }
    }
    return fallback;
}
