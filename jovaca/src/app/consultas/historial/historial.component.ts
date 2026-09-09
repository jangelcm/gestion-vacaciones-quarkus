import { Component, OnDestroy, computed, effect, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ConsultasService } from '../../core/services/consultas.service';
import { ConsultasRealtimeService } from '../../core/services/consultas-realtime.service';
import { SolicitudConsultaDto, SolicitudHistorialDto } from '../../core/models/consulta.model';

@Component({
    selector: 'app-historial-consultas',
    standalone: true,
    imports: [DatePipe, FormsModule],
    templateUrl: './historial.component.html',
    styleUrl: './historial.component.css'
})
export class HistorialComponent implements OnDestroy {
    private auth = inject(AuthService);
    private consultasService = inject(ConsultasService);
    private consultasRealtime = inject(ConsultasRealtimeService);

    esAdmin = computed(() => this.auth.hasRole('Administrador'));

    inputId = this.auth.currentUser()?.id ?? 0;
    solicitudes = signal<SolicitudConsultaDto[]>([]);
    solicitudSeleccionada = signal<SolicitudConsultaDto | null>(null);
    historial = signal<SolicitudHistorialDto[]>([]);

    loadingSolicitudes = signal(false);
    loadingHistorial = signal(false);
    errorSolicitudes = signal<string | null>(null);
    errorHistorial = signal<string | null>(null);

    constructor() {
        this.cargarSolicitudes();
        const colaboradorId = this.auth.currentUser()?.id;
        if (colaboradorId) {
            this.consultasRealtime.conectar(colaboradorId);
        }
        effect(() => {
            const tick = this.consultasRealtime.version();
            if (tick < 1) {
                return;
            }
            const seleccionada = this.solicitudSeleccionada();
            this.cargarSolicitudes(true);
            if (seleccionada) {
                this.verHistorial(seleccionada);
            }
        });
    }

    ngOnDestroy(): void {
        this.consultasRealtime.desconectar();
    }

    cargarSolicitudes(preservarSeleccion = false): void {
        const colaboradorId = this.esAdmin() ? this.inputId : this.auth.currentUser()?.id;
        if (!colaboradorId || colaboradorId < 1) {
            return;
        }

        this.loadingSolicitudes.set(true);
        this.errorSolicitudes.set(null);
        if (!preservarSeleccion) {
            this.solicitudSeleccionada.set(null);
            this.historial.set([]);
        }

        this.consultasService.listarSolicitudesUsuario(colaboradorId).subscribe({
            next: (data) => {
                this.solicitudes.set(data);
                this.loadingSolicitudes.set(false);
            },
            error: () => {
                this.errorSolicitudes.set('No se pudo cargar el historial de solicitudes');
                this.loadingSolicitudes.set(false);
            }
        });
    }

    verHistorial(solicitud: SolicitudConsultaDto): void {
        this.solicitudSeleccionada.set(solicitud);
        this.loadingHistorial.set(true);
        this.errorHistorial.set(null);

        this.consultasService.listarHistorialSolicitud(solicitud.id).subscribe({
            next: (items) => {
                this.historial.set(items);
                this.loadingHistorial.set(false);
            },
            error: () => {
                this.errorHistorial.set('No se pudo cargar el detalle del historial');
                this.loadingHistorial.set(false);
            }
        });
    }

    badgeClass(estado: string): string {
        const map: Record<string, string> = {
            PENDIENTE: 'badge-pendiente',
            APROBADA: 'badge-aprobada',
            RECHAZADA: 'badge-rechazada',
            CANCELADA: 'badge-cancelada'
        };
        return map[estado] ?? '';
    }
}
