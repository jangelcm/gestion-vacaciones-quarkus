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
    private reconectarTimeoutId: ReturnType<typeof setTimeout> | null = null;
    private intentandoConectar = false;
    private static readonly RECONEXION_MS = 5000;

    notificaciones = signal<Notificacion[]>([]);
    conectado = signal(false);
    private ultimaVezVistasMs = signal<number>(0);
    private descartadasIds = new Set<string>();

    noLeidas = computed(() =>
        this.notificaciones().filter(n => parseFechaBackend(n.fechaCreacion).getTime() > this.ultimaVezVistasMs())
            .length
    );

    conectar(colaboradorId: number): void {
        if (this.colaboradorActual === colaboradorId && this.socket) {
            return;
        }
        this.desconectar();
        this.intentandoConectar = true;
        this.colaboradorActual = colaboradorId;
        this.cargarUltimaVezVistas(colaboradorId);
        this.cargarDescartadas(colaboradorId);
        this.cargarHistorial(colaboradorId);
        this.abrirSocket(colaboradorId);
    }

    desconectar(): void {
        this.intentandoConectar = false;
        if (this.reconectarTimeoutId) {
            clearTimeout(this.reconectarTimeoutId);
            this.reconectarTimeoutId = null;
        }
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

    /** Oculta la notificacion de la campanita (boton "Cerrar" o "Ver") y no vuelve a aparecer. */
    descartar(id: string): void {
        this.descartadasIds.add(id);
        this.notificaciones.update(actuales => actuales.filter(n => n.id !== id));
        if (this.colaboradorActual !== null) {
            this.guardarDescartadas(this.colaboradorActual);
        }
    }

    /** A donde navegar al presionar "Ver" segun el origen de la notificacion. */
    rutaDestino(n: Notificacion): string[] {
        switch (n.eventoOrigen) {
            case 'solicitud.creada':
                return ['/aprobaciones'];
            case 'solicitud.aprobada':
            case 'solicitud.rechazada':
            case 'solicitud.cancelada':
                return ['/solicitudes'];
            default:
                return ['/dashboard'];
        }
    }

    private cargarHistorial(colaboradorId: number): void {
        this.http.get<Notificacion[]>(`${this.BASE}/historial/${colaboradorId}`).subscribe({
            next: (data) => {
                const ordenadas = [...data]
                    .filter(n => !this.descartadasIds.has(n.id))
                    .sort((a, b) => parseFechaBackend(b.fechaCreacion).getTime() - parseFechaBackend(a.fechaCreacion).getTime());
                this.notificaciones.set(ordenadas);
            }
        });
    }

    private abrirSocket(colaboradorId: number): void {
        try {
            const socket = new WebSocket(`${this.WS_BASE}/notificaciones/${colaboradorId}`);
            this.socket = socket;
            socket.onopen = () => this.conectado.set(true);
            socket.onclose = () => {
                this.conectado.set(false);
                // La conexion se cayo sin que el usuario cerrara sesion (backend reiniciado,
                // corte de red, etc.): reintenta para no dejar la campanita muda hasta un F5.
                this.programarReconexion(colaboradorId);
            };
            socket.onerror = () => this.conectado.set(false);
            socket.onmessage = () => this.cargarHistorial(colaboradorId);
        } catch {
            this.conectado.set(false);
            this.programarReconexion(colaboradorId);
        }
    }

    private programarReconexion(colaboradorId: number): void {
        if (!this.intentandoConectar || this.colaboradorActual !== colaboradorId || this.reconectarTimeoutId) {
            return;
        }
        this.reconectarTimeoutId = setTimeout(() => {
            this.reconectarTimeoutId = null;
            if (this.intentandoConectar && this.colaboradorActual === colaboradorId) {
                this.abrirSocket(colaboradorId);
            }
        }, NotificacionesService.RECONEXION_MS);
    }

    private cargarUltimaVezVistas(colaboradorId: number): void {
        try {
            const guardado = localStorage.getItem(this.claveStorage(colaboradorId));
            this.ultimaVezVistasMs.set(guardado ? Number(guardado) : 0);
        } catch {
            this.ultimaVezVistasMs.set(0);
        }
    }

    private cargarDescartadas(colaboradorId: number): void {
        try {
            const guardado = localStorage.getItem(this.claveDescartadas(colaboradorId));
            this.descartadasIds = new Set(guardado ? JSON.parse(guardado) : []);
        } catch {
            this.descartadasIds = new Set();
        }
    }

    private guardarDescartadas(colaboradorId: number): void {
        try {
            localStorage.setItem(this.claveDescartadas(colaboradorId), JSON.stringify([...this.descartadasIds]));
        } catch {
            // localStorage no disponible: el descarte solo dura para esta sesion.
        }
    }

    private claveStorage(colaboradorId: number): string {
        return `jovaca_notif_vistas_${colaboradorId}`;
    }

    private claveDescartadas(colaboradorId: number): string {
        return `jovaca_notif_descartadas_${colaboradorId}`;
    }
}
