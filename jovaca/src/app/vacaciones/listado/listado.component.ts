import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SolicitudesService } from '../../services/solicitudes.service';
import { Solicitud } from '../../models/solicitud.model';
import { DatePipe } from '@angular/common';

@Component({
    selector: 'app-listado',
    standalone: true,
    imports: [FormsModule, RouterLink, DatePipe],
    templateUrl: './listado.component.html',
    styleUrl: './listado.component.css'
})
export class ListadoComponent {
    private svc = inject(SolicitudesService);
    private router = inject(Router);

    inputId = 0;
    buscado = signal(false);
    solicitudes = signal<Solicitud[]>([]);
    loading = signal(false);
    error = signal<string | null>(null);

    cargar(): void {
        if (!this.inputId || this.inputId < 1) return;
        this.buscado.set(true);
        this.loading.set(true);
        this.error.set(null);
        this.svc.listar(this.inputId).subscribe({
            next: (data) => { this.solicitudes.set(data); this.loading.set(false); },
            error: () => {
                this.error.set('Error al cargar solicitudes'); this.loading.set(false);
            }
        });
    }

    verDetalle(id: number): void {
        this.router.navigate(['/solicitudes', id]);
    }

    badgeClass(estado: string): string {
        const map: Record<string, string> = {
            PENDIENTE: 'badge-pendiente',
            APROBADA: 'badge-aprobada',
            RECHAZADA: 'badge-rechazada'
        };
        return map[estado] ?? '';
    }
}
