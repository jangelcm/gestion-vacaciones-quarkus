import { Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ConsultasRealtimeService {
    private readonly WS_BASE = environment.apiBaseUrl.replace(/^http/, 'ws');

    private socket: WebSocket | null = null;
    private colaboradorActual: number | null = null;
    private reconectarTimeoutId: ReturnType<typeof setTimeout> | null = null;
    private intentandoConectar = false;
    private static readonly RECONEXION_MS = 5000;

    conectado = signal(false);
    version = signal(0);

    conectar(colaboradorId: number): void {
        if (this.colaboradorActual === colaboradorId && this.socket) {
            return;
        }
        this.desconectar();
        this.intentandoConectar = true;
        this.colaboradorActual = colaboradorId;
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
        this.colaboradorActual = null;
        this.conectado.set(false);
    }

    private abrirSocket(colaboradorId: number): void {
        try {
            const socket = new WebSocket(`${this.WS_BASE}/consultas-updates/${colaboradorId}`);
            this.socket = socket;
            socket.onopen = () => this.conectado.set(true);
            socket.onclose = () => {
                this.conectado.set(false);
                this.programarReconexion(colaboradorId);
            };
            socket.onerror = () => this.conectado.set(false);
            socket.onmessage = () => this.version.update(v => v + 1);
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
        }, ConsultasRealtimeService.RECONEXION_MS);
    }
}
