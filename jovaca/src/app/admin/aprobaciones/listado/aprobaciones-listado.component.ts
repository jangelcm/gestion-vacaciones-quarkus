import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { map, switchMap } from 'rxjs';
import { AprobacionesService } from '../../../core/services/aprobaciones.service';
import { AuthService } from '../../../core/services/auth.service';
import { UsuariosService } from '../../../core/services/usuarios.service';
import { ModalComponent } from '../../../shared/modal/modal.component';
import { Aprobacion } from '../../../core/models/aprobacion.model';

interface PendienteView {
    aprobacion: Aprobacion;
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
        this.aprobService.listarPendientes().pipe(
            switchMap(pendientes => {
                const ids = [...new Set(
                    pendientes
                        .map(p => Number(p.colaboradorId))
                        .filter(id => !Number.isNaN(id))
                )];
                return this.usuariosSvc.listarPorIds(ids).pipe(
                    map(usuarios => ({ pendientes, usuarios }))
                );
            })
        ).subscribe({
            next: ({ pendientes, usuarios }) => {
                this.nombresPorId.set(new Map(usuarios.map(u => [u.id, u.username])));
                this.pendientes.set(pendientes.map((aprobacion): PendienteView => ({ aprobacion })));
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
        return v && v.aprobacion.colaboradorId ? this.nombreColaborador(v.aprobacion.colaboradorId) : '';
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
        const solicitudId = v.aprobacion.solicitudId;

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
