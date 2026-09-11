import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ConsultasService } from '../../core/services/consultas.service';
import { SolicitudConsultaDto } from '../../core/models/consulta.model';
import { AuthService } from '../../core/services/auth.service';
import { extraerMensajeError } from '../../core/utils/error.util';

@Component({
    selector: 'app-listado',
    standalone: true,
    imports: [DatePipe],
    templateUrl: './listado.component.html',
    styleUrl: './listado.component.css'
})
export class ListadoComponent {
    private svc = inject(ConsultasService);
    private auth = inject(AuthService);

    solicitudes = signal<SolicitudConsultaDto[]>([]);
    loading = signal(false);
    error = signal<string | null>(null);

    constructor() {
        this.cargar();
    }

    cargar(): void {
        const id = this.auth.currentUser()?.id;
        if (!id || id < 1) {
            this.error.set('No se pudo identificar el usuario autenticado.');
            return;
        }
        this.loading.set(true);
        this.error.set(null);
        this.svc.listarSolicitudesUsuario(id).subscribe({
            next: (data) => { this.solicitudes.set(data); this.loading.set(false); },
            error: (err) => { this.error.set(extraerMensajeError(err, 'Error al cargar solicitudes')); this.loading.set(false); }
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
