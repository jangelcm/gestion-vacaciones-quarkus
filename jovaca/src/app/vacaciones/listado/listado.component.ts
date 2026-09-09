import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { SolicitudesService } from '../../services/solicitudes.service';
import { Solicitud } from '../../models/solicitud.model';
import { AuthService } from '../../core/services/auth.service';

@Component({
    selector: 'app-listado',
    standalone: true,
    imports: [DatePipe],
    templateUrl: './listado.component.html',
    styleUrl: './listado.component.css'
})
export class ListadoComponent {
    private svc = inject(SolicitudesService);
    private auth = inject(AuthService);

    solicitudes = signal<Solicitud[]>([]);
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
        this.svc.listar(id).subscribe({
            next: (data) => { this.solicitudes.set(data); this.loading.set(false); },
            error: () => { this.error.set('Error al cargar solicitudes'); this.loading.set(false); }
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
