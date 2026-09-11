import { Component, computed, inject, signal } from '@angular/core';
import { switchMap, map } from 'rxjs';
import { ConsultasService } from '../../core/services/consultas.service';
import { UsuariosService } from '../../core/services/usuarios.service';
import { SolicitudConsultaDto } from '../../core/models/consulta.model';
import { extraerMensajeError } from '../../core/utils/error.util';

interface BarraSolicitud {
    solicitud: SolicitudConsultaDto;
    columnaInicio: number;
    columnaFin: number;
}

interface FilaColaborador {
    colaboradorId: string;
    nombre: string;
    barras: BarraSolicitud[];
}

const NOMBRES_MES = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
];

@Component({
    selector: 'app-calendario-equipo',
    standalone: true,
    templateUrl: './calendario-equipo.component.html',
    styleUrl: './calendario-equipo.component.css'
})
export class CalendarioEquipoComponent {
    private consultasService = inject(ConsultasService);
    private usuariosService = inject(UsuariosService);

    private hoy = new Date();
    mesActual = signal(new Date(this.hoy.getFullYear(), this.hoy.getMonth(), 1));
    filas = signal<FilaColaborador[]>([]);
    loading = signal(false);
    error = signal<string | null>(null);

    tituloMes = computed(() => {
        const m = this.mesActual();
        return `${NOMBRES_MES[m.getMonth()]} ${m.getFullYear()}`;
    });

    diasDelMes = computed(() => {
        const m = this.mesActual();
        return new Date(m.getFullYear(), m.getMonth() + 1, 0).getDate();
    });

    numerosDia = computed(() => Array.from({ length: this.diasDelMes() }, (_, i) => i + 1));

    constructor() {
        this.cargar();
    }

    private cargar(): void {
        const m = this.mesActual();
        const desde = this.aIsoFecha(new Date(m.getFullYear(), m.getMonth(), 1));
        const hasta = this.aIsoFecha(new Date(m.getFullYear(), m.getMonth() + 1, 0));

        this.loading.set(true);
        this.error.set(null);

        this.consultasService.listarSolicitudesEnRango(desde, hasta).pipe(
            switchMap(solicitudes => {
                const ids = [...new Set(
                    solicitudes.map(s => Number(s.colaboradorId)).filter(id => !Number.isNaN(id))
                )];
                return this.usuariosService.listarPorIds(ids).pipe(
                    map(usuarios => ({ solicitudes, usuarios }))
                );
            })
        ).subscribe({
            next: ({ solicitudes, usuarios }) => {
                const nombresPorId = new Map(usuarios.map(u => [u.id, u.username]));
                this.filas.set(this.construirFilas(solicitudes, nombresPorId, m));
                this.loading.set(false);
            },
            error: (err) => {
                this.error.set(extraerMensajeError(err, 'No se pudo cargar el calendario del equipo'));
                this.loading.set(false);
            }
        });
    }

    mesAnterior(): void {
        const m = this.mesActual();
        this.mesActual.set(new Date(m.getFullYear(), m.getMonth() - 1, 1));
        this.cargar();
    }

    mesSiguiente(): void {
        const m = this.mesActual();
        this.mesActual.set(new Date(m.getFullYear(), m.getMonth() + 1, 1));
        this.cargar();
    }

    irAHoy(): void {
        this.mesActual.set(new Date(this.hoy.getFullYear(), this.hoy.getMonth(), 1));
        this.cargar();
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

    private construirFilas(
        solicitudes: SolicitudConsultaDto[],
        nombresPorId: Map<number, string>,
        mesVisible: Date
    ): FilaColaborador[] {
        const diasMes = new Date(mesVisible.getFullYear(), mesVisible.getMonth() + 1, 0).getDate();
        const inicioMes = new Date(mesVisible.getFullYear(), mesVisible.getMonth(), 1);
        const finMes = new Date(mesVisible.getFullYear(), mesVisible.getMonth(), diasMes);

        const porColaborador = new Map<string, SolicitudConsultaDto[]>();
        for (const s of solicitudes) {
            const lista = porColaborador.get(s.colaboradorId) ?? [];
            lista.push(s);
            porColaborador.set(s.colaboradorId, lista);
        }

        return [...porColaborador.entries()]
            .map(([colaboradorId, lista]): FilaColaborador => {
                const idNum = Number(colaboradorId);
                const nombre = nombresPorId.get(idNum) ?? `Colaborador #${colaboradorId}`;
                const barras = lista.map((solicitud): BarraSolicitud => {
                    const inicio = this.maxFecha(this.aFecha(solicitud.fechaInicio), inicioMes);
                    const fin = this.minFecha(this.aFecha(solicitud.fechaFin), finMes);
                    return {
                        solicitud,
                        columnaInicio: inicio.getDate(),
                        columnaFin: fin.getDate()
                    };
                });
                return { colaboradorId, nombre, barras };
            })
            .sort((a, b) => a.nombre.localeCompare(b.nombre));
    }

    private aFecha(iso: string): Date {
        const [anio, mes, dia] = iso.slice(0, 10).split('-').map(Number);
        return new Date(anio, mes - 1, dia);
    }

    private maxFecha(a: Date, b: Date): Date {
        return a > b ? a : b;
    }

    private minFecha(a: Date, b: Date): Date {
        return a < b ? a : b;
    }

    private aIsoFecha(fecha: Date): string {
        const anio = fecha.getFullYear();
        const mes = String(fecha.getMonth() + 1).padStart(2, '0');
        const dia = String(fecha.getDate()).padStart(2, '0');
        return `${anio}-${mes}-${dia}`;
    }
}
