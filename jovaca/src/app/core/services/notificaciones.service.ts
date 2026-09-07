import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Notificacion } from '../models/notificacion.model';

/** Convierte "dd-MM-yyyy HH:mm:ss" (formato del backend) a Date. */
function parseFechaBackend(fecha: string): Date {
    const [datePart, timePart] = fecha.split(' ');
    const [day, month, year] = datePart.split('-').map(Number);
    const [hour, minute, second] = (timePart ?? '00:00:00').split(':').map(Number);
    return new Date(year, month - 1, day, hour, minute, second);
}

@Injectable({ providedIn: 'root' })
export class NotificacionesService {
    private readonly BASE = `${environment.apiBaseUrl}/notificaciones`;
    private readonly WS_BASE = environment.apiBaseUrl.replace(/^http/, 'ws');
    private http = inject(HttpClient);

    private socket: WebSocket | null = null;
    private colaboradorActual: number | null = null;

    notificaciones = signal<Notificacion[]>([]);
    conectado = signal(false);
    private ultimaVezVistasMs = signal<number>(0);

    noLeidas = computed(() =>
        this.notificaciones().filter(n => parseFechaBackend(n.fechaCreacion).getTime() > this.ultimaVezVistasMs())
            .length
    );

    conectar(colaboradorId: number): void {
        if (this.colaboradorActual === colaboradorId && this.socket) {
            return;
        }
        this.desconectar();
        this.colaboradorActual = colaboradorId;
        this.cargarUltimaVezVistas(colaboradorId);
        this.cargarHistorial(colaboradorId);
        this.abrirSocket(colaboradorId);
    }

    desconectar(): void {
        this.socket?.close();
        this.socket = null;
        this.conectado.set(false);
    }

    marcarComoVistas(): void {
        if (this.colaboradorActual === null) return;
        const ahora = Date.now();
        this.ultimaVezVistasMs.set(ahora);
        try {
            localStorage.setItem(this.claveStorage(this.colaboradorActual), String(ahora));
        } catch {
            // localStorage no disponible: el estado sigue funcionando en memoria para esta sesion.
        }
    }

    private cargarHistorial(colaboradorId: number): void {
        this.http.get<Notificacion[]>(`${this.BASE}/historial/${colaboradorId}`).subscribe({
            next: (data) => {
                const ordenadas = [...data].sort(
                    (a, b) => parseFechaBackend(b.fechaCreacion).getTime() - parseFechaBackend(a.fechaCreacion).getTime()
                );
                this.notificaciones.set(ordenadas);
            }
        });
    }

    private abrirSocket(colaboradorId: number): void {
        try {
            this.socket = new WebSocket(`${this.WS_BASE}/notificaciones/${colaboradorId}`);
            this.socket.onopen = () => this.conectado.set(true);
            this.socket.onclose = () => this.conectado.set(false);
            this.socket.onerror = () => this.conectado.set(false);
            this.socket.onmessage = () => this.cargarHistorial(colaboradorId);
        } catch {
            this.conectado.set(false);
        }
    }

    private cargarUltimaVezVistas(colaboradorId: number): void {
        try {
            const guardado = localStorage.getItem(this.claveStorage(colaboradorId));
            this.ultimaVezVistasMs.set(guardado ? Number(guardado) : 0);
        } catch {
            this.ultimaVezVistasMs.set(0);
        }
    }

    private claveStorage(colaboradorId: number): string {
        return `jovaca_notif_vistas_${colaboradorId}`;
    }
}
