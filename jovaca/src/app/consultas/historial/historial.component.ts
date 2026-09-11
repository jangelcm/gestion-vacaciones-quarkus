import { Component, OnDestroy, computed, effect, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { map, switchMap } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { ConsultasService } from '../../core/services/consultas.service';
import { ConsultasRealtimeService } from '../../core/services/consultas-realtime.service';
import { UsuariosService } from '../../core/services/usuarios.service';
import { SolicitudConsultaDto, SolicitudHistorialDto } from '../../core/models/consulta.model';
import { Usuario } from '../../core/models/usuario.model';
import { extraerMensajeError } from '../../core/utils/error.util';

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
    private usuariosSvc = inject(UsuariosService);

    esAdmin = computed(() => this.auth.hasRole('Administrador'));

    solicitudes = signal<SolicitudConsultaDto[]>([]);
    solicitudSeleccionada = signal<SolicitudConsultaDto | null>(null);
    historial = signal<SolicitudHistorialDto[]>([]);

    loadingSolicitudes = signal(false);
    loadingHistorial = signal(false);
    errorSolicitudes = signal<string | null>(null);
    errorHistorial = signal<string | null>(null);
    yaBusco = signal(false);

    // Búsqueda por nombre (solo admin): lista completa de usuarios cargada una vez,
    // filtrada en el cliente a medida que se escribe.
    private usuarios = signal<Usuario[]>([]);
    nombreBusqueda = '';
    colaboradorSeleccionado = signal<Usuario | null>(null);
    mostrarSugerencias = signal(false);
    // Signal en vez de computed(): nombreBusqueda es una propiedad plana (atada con
    // [(ngModel)]), no una signal, así que un computed() nunca detectaría sus cambios.
    // Se recalcula a mano en cada tecleo (ver (input) en el template).
    sugerencias = signal<Usuario[]>([]);

    actualizarSugerencias(): void {
        const texto = this.nombreBusqueda.trim().toLowerCase();
        if (!texto) {
            this.sugerencias.set([]);
            return;
        }
        this.sugerencias.set(
            this.usuarios().filter(u => u.username.toLowerCase().includes(texto)).slice(0, 8)
        );
    }

    desde = '';
    hasta = '';

    // Modo "todos los colaboradores" (solo fechas, sin colaborador elegido): hay que
    // mostrar de quién es cada fila.
    mostrandoTodos = signal(false);
    private nombresPorId = signal<Map<number, string>>(new Map());

    constructor() {
        const colaboradorId = this.auth.currentUser()?.id;
        if (colaboradorId) {
            this.consultasRealtime.conectar(colaboradorId);
        }

        if (this.esAdmin()) {
            this.usuariosSvc.listarUsuarios().subscribe({
                next: (page) => {
                    this.usuarios.set(page.content);
                    this.actualizarSugerencias();
                },
                error: (err) => this.errorSolicitudes.set(
                    extraerMensajeError(err, 'No se pudo cargar la lista de colaboradores para buscar por nombre'))
            });
        } else {
            this.cargarSolicitudes();
        }

        effect(() => {
            const tick = this.consultasRealtime.version();
            if (tick < 1) {
                return;
            }
            const seleccionada = this.solicitudSeleccionada();
            if (this.esAdmin() && !this.yaBusco()) {
                return;
            }
            this.cargarSolicitudes(true);
            if (seleccionada) {
                this.verHistorial(seleccionada);
            }
        });
    }

    ngOnDestroy(): void {
        this.consultasRealtime.desconectar();
    }

    seleccionarColaborador(u: Usuario): void {
        this.colaboradorSeleccionado.set(u);
        this.nombreBusqueda = u.username;
        this.mostrarSugerencias.set(false);
    }

    limpiarColaborador(): void {
        this.colaboradorSeleccionado.set(null);
        this.nombreBusqueda = '';
        this.sugerencias.set([]);
    }

    limpiarFiltros(): void {
        this.limpiarColaborador();
        this.desde = '';
        this.hasta = '';
        this.solicitudes.set([]);
        this.solicitudSeleccionada.set(null);
        this.historial.set([]);
        this.yaBusco.set(false);
    }

    buscar(): void {
        // Si se escribió un nombre pero no se hizo click en una sugerencia, toma la
        // primera coincidencia en vez de obligar a seleccionarla explícitamente.
        if (!this.colaboradorSeleccionado() && this.nombreBusqueda.trim()) {
            const candidatos = this.sugerencias();
            if (candidatos.length === 0) {
                this.errorSolicitudes.set(`No se encontró ningún colaborador que coincida con "${this.nombreBusqueda.trim()}".`);
                return;
            }
            this.seleccionarColaborador(candidatos[0]);
        }

        const colaborador = this.colaboradorSeleccionado();
        if (!colaborador && (!this.desde || !this.hasta)) {
            this.errorSolicitudes.set(
                'Elegí un colaborador de la lista, o completá "Desde" y "Hasta" para buscar por fecha en todos los colaboradores.');
            return;
        }
        this.cargarSolicitudes();
    }

    cargarSolicitudes(preservarSeleccion = false): void {
        const esAdmin = this.esAdmin();
        const colaborador = this.colaboradorSeleccionado();
        const colaboradorId = esAdmin ? colaborador?.id : this.auth.currentUser()?.id;

        this.loadingSolicitudes.set(true);
        this.errorSolicitudes.set(null);
        this.yaBusco.set(true);
        if (!preservarSeleccion) {
            this.solicitudSeleccionada.set(null);
            this.historial.set([]);
        }

        if (esAdmin && !colaborador) {
            // Sin colaborador elegido: busca por rango de fechas en todos los colaboradores.
            this.mostrandoTodos.set(true);
            this.consultasService.listarSolicitudesEnRango(this.desde, this.hasta).pipe(
                switchMap(solicitudes => {
                    const ids = [...new Set(solicitudes.map(s => Number(s.colaboradorId)).filter(id => !Number.isNaN(id)))];
                    return this.usuariosSvc.listarPorIds(ids).pipe(map(usuarios => ({ solicitudes, usuarios })));
                })
            ).subscribe({
                next: ({ solicitudes, usuarios }) => {
                    this.nombresPorId.set(new Map(usuarios.map(u => [u.id, u.username])));
                    this.solicitudes.set(solicitudes);
                    this.loadingSolicitudes.set(false);
                },
                error: (err) => {
                    this.errorSolicitudes.set(extraerMensajeError(err, 'No se pudo cargar el historial de solicitudes'));
                    this.loadingSolicitudes.set(false);
                }
            });
            return;
        }

        if (!colaboradorId || colaboradorId < 1) {
            this.loadingSolicitudes.set(false);
            return;
        }

        this.mostrandoTodos.set(false);
        this.consultasService.listarSolicitudesUsuario(colaboradorId).subscribe({
            next: (data) => {
                this.solicitudes.set(this.filtrarPorFecha(data));
                this.loadingSolicitudes.set(false);
            },
            error: (err) => {
                this.errorSolicitudes.set(extraerMensajeError(err, 'No se pudo cargar el historial de solicitudes'));
                this.loadingSolicitudes.set(false);
            }
        });
    }

    private filtrarPorFecha(solicitudes: SolicitudConsultaDto[]): SolicitudConsultaDto[] {
        if (!this.desde && !this.hasta) return solicitudes;
        return solicitudes.filter(s => {
            const fecha = s.fechaInicio;
            if (this.desde && fecha < this.desde) return false;
            if (this.hasta && fecha > this.hasta) return false;
            return true;
        });
    }

    nombreColaborador(colaboradorId: string | number): string {
        return this.nombresPorId().get(Number(colaboradorId)) ?? `#${colaboradorId}`;
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
            error: (err) => {
                this.errorHistorial.set(extraerMensajeError(err, 'No se pudo cargar el detalle del historial'));
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
