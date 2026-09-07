import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { SolicitudesService } from '../../services/solicitudes.service';
import { Solicitud } from '../../models/solicitud.model';
import { AuthService } from '../../core/services/auth.service';
import { UsuariosService } from '../../core/services/usuarios.service';
import { PoliticasService } from '../../core/services/politicas.service';
import { SaldoDias } from '../../core/models/politica.model';
import { ModalComponent } from '../../shared/modal/modal.component';
import { FormularioComponent } from '../formulario/formulario.component';
import { DetalleComponent } from '../detalle/detalle.component';

@Component({
    selector: 'app-listado',
    standalone: true,
    imports: [FormsModule, DatePipe, ModalComponent, FormularioComponent, DetalleComponent],
    templateUrl: './listado.component.html',
    styleUrl: './listado.component.css'
})
export class ListadoComponent {
    private svc = inject(SolicitudesService);
    private auth = inject(AuthService);
    private usuariosSvc = inject(UsuariosService);
    private politicasSvc = inject(PoliticasService);

    esAdmin = computed(() => this.auth.hasRole('Administrador'));

    inputId = this.auth.currentUser()?.id ?? 0;
    solicitudes = signal<Solicitud[]>([]);
    loading = signal(false);
    error = signal<string | null>(null);

    modalNuevaAbierta = signal(false);
    solicitudDetalle = signal<Solicitud | null>(null);

    miSaldo = signal<SaldoDias | null>(null);
    miSaldoError = signal<string | null>(null);
    miSaldoLoading = signal(false);

    private nombresPorId = signal<Map<number, string>>(new Map());

    constructor() {
        this.cargar();
        this.cargarMiSaldo();
        if (this.esAdmin()) {
            this.usuariosSvc.listarUsuarios().subscribe({
                next: (res) => {
                    this.nombresPorId.set(new Map(res.content.map(u => [u.id, u.username])));
                }
            });
        }
    }

    cargarMiSaldo(): void {
        const miId = this.auth.currentUser()?.id;
        if (!miId) return;
        this.miSaldoLoading.set(true);
        this.miSaldoError.set(null);
        this.politicasSvc.obtenerSaldo(miId).subscribe({
            next: (s) => { this.miSaldo.set(s); this.miSaldoLoading.set(false); },
            error: () => {
                this.miSaldo.set(null);
                this.miSaldoError.set('Todavía no tienes una política de vacaciones asignada');
                this.miSaldoLoading.set(false);
            }
        });
    }

    nombreColaborador(colaboradorId: string | number): string {
        return this.nombresPorId().get(Number(colaboradorId)) ?? `#${colaboradorId}`;
    }

    verTodas = signal(false);

    cargar(): void {
        const id = this.esAdmin() ? this.inputId : this.auth.currentUser()?.id;
        if (!id || id < 1) return;
        this.verTodas.set(false);
        this.loading.set(true);
        this.error.set(null);
        this.svc.listar(id).subscribe({
            next: (data) => { this.solicitudes.set(data); this.loading.set(false); },
            error: () => { this.error.set('Error al cargar solicitudes'); this.loading.set(false); }
        });
    }

    cargarTodas(): void {
        this.verTodas.set(true);
        this.loading.set(true);
        this.error.set(null);
        this.svc.listarTodas().subscribe({
            next: (data) => { this.solicitudes.set(data); this.loading.set(false); },
            error: () => { this.error.set('Error al cargar solicitudes'); this.loading.set(false); }
        });
    }

    verDetalle(s: Solicitud): void {
        this.solicitudDetalle.set(s);
    }

    cerrarDetalle(): void {
        this.solicitudDetalle.set(null);
    }

    abrirModalNueva(): void {
        this.modalNuevaAbierta.set(true);
    }

    cerrarModalNueva(): void {
        this.modalNuevaAbierta.set(false);
    }

    onSolicitudCreada(): void {
        this.modalNuevaAbierta.set(false);
        if (this.verTodas()) {
            this.cargarTodas();
        } else {
            this.cargar();
        }
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
