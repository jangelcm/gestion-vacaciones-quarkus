import { Component, inject, signal } from '@angular/core';
import { ReporteResumen, ReportesService } from '../../../core/services/reportes.service';
import { extraerMensajeError } from '../../../core/utils/error.util';

@Component({
    selector: 'app-reportes-resumen',
    standalone: true,
    templateUrl: './reportes-resumen.component.html',
    styleUrl: './reportes-resumen.component.css'
})
export class ReportesResumenComponent {
    private reportesService = inject(ReportesService);

    resumen = signal<ReporteResumen | null>(null);
    loading = signal(false);
    error = signal<string | null>(null);

    constructor() {
        this.cargar();
    }

    cargar(): void {
        this.loading.set(true);
        this.error.set(null);
        this.reportesService.obtenerResumen().subscribe({
            next: (data) => {
                this.resumen.set(data);
                this.loading.set(false);
            },
            error: (err) => {
                this.error.set(extraerMensajeError(err, 'No se pudo cargar el resumen de reportes'));
                this.loading.set(false);
            }
        });
    }
}
