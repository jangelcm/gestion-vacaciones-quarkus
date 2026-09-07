import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { AprobacionesService } from '../../../core/services/aprobaciones.service';
import { SolicitudesService } from '../../../services/solicitudes.service';
import { AuthService } from '../../../core/services/auth.service';
import { UsuariosService } from '../../../core/services/usuarios.service';
import { ModalComponent } from '../../../shared/modal/modal.component';
import { Aprobacion } from '../../../core/models/aprobacion.model';
import { Solicitud } from '../../../models/solicitud.model';

interface PendienteView {
    aprobacion: Aprobacion;
    solicitud: Solicitud;
}

type Accion = 'aprobar' | 'rechazar';

@Component({
    selector: 'app-aprobaciones-listado',
    standalone: true,
    imports: [FormsModule, DatePipe, ModalComponent],
    templateUrl: './aprobaciones-listado.component.html',
    styleUrl: './aprobaciones-listado.component.css'
})
export class AprobacionesListadoComponent implements OnInit {
    private aprobService = inject(AprobacionesService);
    private solService = inject(SolicitudesService);
    private auth = inject(AuthService);
    private usuariosSvc = inject(UsuariosService);

    pendientes = signal<PendienteView[]>([]);
    loading = signal(false);
    error = signal<string | null>(null);

    private nombresPorId = signal<Map<number, string>>(new Map());

    modalAccion = signal<Accion | null>(null);
    solicitudSeleccionada = signal<PendienteView | null>(null);
    comentario = '';
    procesando = signal(false);
    accionError = signal<string | null>(null);

    ngOnInit(): void {
        this.cargar();
    }

    cargar(): void {
        this.loading.set(true);
        this.error.set(null);
        forkJoin({
            pendientes: this.aprobService.listarPendientes(),
            solicitudes: this.solService.listarTodas(),
            usuarios: this.usuariosSvc.listarUsuarios()
        }).subscribe({
            next: ({ pendientes, solicitudes, usuarios }) => {
                this.nombresPorId.set(new Map(usuarios.content.map(u => [u.id, u.username])));

                const mapa = new Map(solicitudes.map(s => [s.id, s]));
                const vistas = pendientes
                    .map((p): PendienteView | null => {
                        const s = mapa.get(p.solicitudId);
                        return s ? { aprobacion: p, solicitud: s } : null;
                    })
                    .filter((v): v is PendienteView => v !== null);
                this.pendientes.set(vistas);
                this.loading.set(false);
            },
            error: () => {
                this.error.set('No se pudieron cargar las solicitudes pendientes de aprobación');
                this.loading.set(false);
            }
        });
    }

    nombreColaborador(colaboradorId: string | number): string {
        return this.nombresPorId().get(Number(colaboradorId)) ?? `#${colaboradorId}`;
    }

    nombreColaboradorSeleccionado = computed(() => {
        const v = this.solicitudSeleccionada();
        return v ? this.nombreColaborador(v.solicitud.colaboradorId) : '';
    });

    abrirAprobar(v: PendienteView): void {
        this.solicitudSeleccionada.set(v);
        this.comentario = '';
        this.accionError.set(null);
        this.modalAccion.set('aprobar');
    }

    abrirRechazar(v: PendienteView): void {
        this.solicitudSeleccionada.set(v);
        this.comentario = '';
        this.accionError.set(null);
        this.modalAccion.set('rechazar');
    }

    cerrarModal(): void {
        this.modalAccion.set(null);
        this.solicitudSeleccionada.set(null);
    }

    confirmar(): void {
        const v = this.solicitudSeleccionada();
        const accion = this.modalAccion();
        const aprobadorId = this.auth.currentUser()?.id != null ? String(this.auth.currentUser()!.id) : null;

        if (!v || !accion || !aprobadorId) {
            this.accionError.set('No se pudo determinar el usuario aprobador de la sesión actual');
            return;
        }
        if (accion === 'rechazar' && !this.comentario.trim()) {
            this.accionError.set('El motivo del rechazo es obligatorio');
            return;
        }

        this.procesando.set(true);
        this.accionError.set(null);
        const solicitudId = v.solicitud.id;

        const obs = accion === 'aprobar'
            ? this.aprobService.aprobar(solicitudId, { aprobadorId, comentario: this.comentario })
            : this.aprobService.rechazar(solicitudId, { aprobadorId, motivo: this.comentario });

        obs.subscribe({
            next: () => {
                this.procesando.set(false);
                this.cerrarModal();
                this.cargar();
            },
            error: () => {
                this.procesando.set(false);
                this.accionError.set('No se pudo procesar la acción. Intente nuevamente.');
            }
        });
    }
}
