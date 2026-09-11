import { Component, computed, inject, signal } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { ConsultasService } from '../../core/services/consultas.service';
import { SolicitudConsultaDto } from '../../core/models/consulta.model';
import { extraerMensajeError } from '../../core/utils/error.util';

interface DiaCalendario {
    fecha: Date;
    numero: number;
    esDelMesActual: boolean;
    esHoy: boolean;
    solicitud: SolicitudConsultaDto | null;
}

const NOMBRES_MES = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
];
const NOMBRES_DIA = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

@Component({
    selector: 'app-calendario-mensual',
    standalone: true,
    templateUrl: './calendario-mensual.component.html',
    styleUrl: './calendario-mensual.component.css'
})
export class CalendarioMensualComponent {
    private auth = inject(AuthService);
    private consultasService = inject(ConsultasService);

    private hoy = new Date();
    mesActual = signal(new Date(this.hoy.getFullYear(), this.hoy.getMonth(), 1));
    solicitudes = signal<SolicitudConsultaDto[]>([]);
    loading = signal(false);
    error = signal<string | null>(null);

    nombresDia = NOMBRES_DIA;

    tituloMes = computed(() => {
        const m = this.mesActual();
        return `${NOMBRES_MES[m.getMonth()]} ${m.getFullYear()}`;
    });

    dias = computed<DiaCalendario[]>(() => {
        const m = this.mesActual();
        const anio = m.getFullYear();
        const mes = m.getMonth();
        const primerDiaMes = new Date(anio, mes, 1);
        // Lunes = 0 ... Domingo = 6
        const offsetInicio = (primerDiaMes.getDay() + 6) % 7;
        const inicioGrilla = new Date(anio, mes, 1 - offsetInicio);

        const sols = this.solicitudes();
        const celdas: DiaCalendario[] = [];
        for (let i = 0; i < 42; i++) {
            const fecha = new Date(inicioGrilla.getFullYear(), inicioGrilla.getMonth(), inicioGrilla.getDate() + i);
            celdas.push({
                fecha,
                numero: fecha.getDate(),
                esDelMesActual: fecha.getMonth() === mes,
                esHoy: this.esMismoDia(fecha, this.hoy),
                solicitud: this.buscarSolicitudParaFecha(sols, fecha)
            });
        }
        return celdas;
    });

    constructor() {
        this.cargar();
    }

    private cargar(): void {
        const colaboradorId = this.auth.currentUser()?.id;
        if (!colaboradorId) {
            return;
        }
        this.loading.set(true);
        this.error.set(null);
        this.consultasService.listarSolicitudesUsuario(colaboradorId).subscribe({
            next: (data) => {
                this.solicitudes.set(data);
                this.loading.set(false);
            },
            error: (err) => {
                this.error.set(extraerMensajeError(err, 'No se pudo cargar tu calendario de vacaciones'));
                this.loading.set(false);
            }
        });
    }

    mesAnterior(): void {
        const m = this.mesActual();
        this.mesActual.set(new Date(m.getFullYear(), m.getMonth() - 1, 1));
    }

    mesSiguiente(): void {
        const m = this.mesActual();
        this.mesActual.set(new Date(m.getFullYear(), m.getMonth() + 1, 1));
    }

    irAHoy(): void {
        this.mesActual.set(new Date(this.hoy.getFullYear(), this.hoy.getMonth(), 1));
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

    private buscarSolicitudParaFecha(solicitudes: SolicitudConsultaDto[], fecha: Date): SolicitudConsultaDto | null {
        const fechaStr = this.aIsoFecha(fecha);
        return solicitudes.find(s => fechaStr >= s.fechaInicio.slice(0, 10) && fechaStr <= s.fechaFin.slice(0, 10)) ?? null;
    }

    private esMismoDia(a: Date, b: Date): boolean {
        return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
    }

    private aIsoFecha(fecha: Date): string {
        const anio = fecha.getFullYear();
        const mes = String(fecha.getMonth() + 1).padStart(2, '0');
        const dia = String(fecha.getDate()).padStart(2, '0');
        return `${anio}-${mes}-${dia}`;
    }
}
