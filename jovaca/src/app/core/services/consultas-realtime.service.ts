import { Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ConsultasRealtimeService {
    private readonly WS_BASE = environment.apiBaseUrl.replace(/^http/, 'ws');

    private socket: WebSocket | null = null;
    private colaboradorActual: number | null = null;

    conectado = signal(false);
    version = signal(0);

    conectar(colaboradorId: number): void {
        if (this.colaboradorActual === colaboradorId && this.socket) {
            return;
        }
        this.desconectar();
        this.colaboradorActual = colaboradorId;

        try {
            this.socket = new WebSocket(`${this.WS_BASE}/consultas-updates/${colaboradorId}`);
            this.socket.onopen = () => this.conectado.set(true);
            this.socket.onclose = () => this.conectado.set(false);
            this.socket.onerror = () => this.conectado.set(false);
            this.socket.onmessage = () => this.version.update(v => v + 1);
        } catch {
            this.conectado.set(false);
        }
    }

    desconectar(): void {
        this.socket?.close();
        this.socket = null;
        this.colaboradorActual = null;
        this.conectado.set(false);
    }
}
