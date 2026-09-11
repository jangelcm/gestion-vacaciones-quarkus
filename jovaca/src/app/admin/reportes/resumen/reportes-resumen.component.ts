import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { map, switchMap } from 'rxjs';
import { ReporteResumen, ReportesService, SaldoReporteDto } from '../../../core/services/reportes.service';
import { ConsultasService } from '../../../core/services/consultas.service';
import { UsuariosService } from '../../../core/services/usuarios.service';
import { PoliticaConsultaDto } from '../../../core/models/consulta.model';
import { extraerMensajeError } from '../../../core/utils/error.util';

@Component({
    selector: 'app-reportes-resumen',
    standalone: true,
    imports: [FormsModule],
    templateUrl: './reportes-resumen.component.html',
    styleUrl: './reportes-resumen.component.css'
})
export class ReportesResumenComponent {
    private reportesService = inject(ReportesService);
    private consultasSvc = inject(ConsultasService);
    private usuariosSvc = inject(UsuariosService);

    resumen = signal<ReporteResumen | null>(null);
    loading = signal(false);
    error = signal<string | null>(null);

    desde = '';
    hasta = '';

    politicas = signal<PoliticaConsultaDto[]>([]);
    politicaIdFiltro: number | null = null;
    saldos = signal<SaldoReporteDto[]>([]);
    loadingSaldos = signal(false);
    errorSaldos = signal<string | null>(null);
    private nombresPorId = signal<Map<number, string>>(new Map());

    constructor() {
        this.cargar();
        this.cargarSaldos();
        this.consultasSvc.listarPoliticas().subscribe({ next: (data) => this.politicas.set(data) });
    }

    cargar(): void {
        this.loading.set(true);
        this.error.set(null);
        this.reportesService.obtenerResumen(this.desde || undefined, this.hasta || undefined).subscribe({
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

    limpiarFiltro(): void {
        this.desde = '';
        this.hasta = '';
        this.cargar();
    }

    cargarSaldos(): void {
        this.loadingSaldos.set(true);
        this.errorSaldos.set(null);
        this.reportesService.obtenerSaldos(this.politicaIdFiltro ?? undefined).pipe(
            switchMap(saldos => {
                const ids = [...new Set(saldos.map(s => s.colaboradorId))];
                return this.usuariosSvc.listarPorIds(ids).pipe(map(usuarios => ({ saldos, usuarios })));
            })
        ).subscribe({
            next: ({ saldos, usuarios }) => {
                this.nombresPorId.set(new Map(usuarios.map(u => [u.id, u.username])));
                this.saldos.set(saldos);
                this.loadingSaldos.set(false);
            },
            error: (err) => {
                this.errorSaldos.set(extraerMensajeError(err, 'No se pudo cargar el saldo por colaborador'));
                this.loadingSaldos.set(false);
            }
        });
    }

    nombreColaborador(colaboradorId: number): string {
        return this.nombresPorId().get(colaboradorId) ?? `#${colaboradorId}`;
    }
}
