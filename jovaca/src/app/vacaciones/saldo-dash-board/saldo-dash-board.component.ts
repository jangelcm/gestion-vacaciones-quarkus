import { Component, OnDestroy, inject, signal, computed } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { PoliticasService } from '../../core/services/politicas.service';
import { SolicitudesService } from '../../services/solicitudes.service';
import { AuthService } from '../../core/services/auth.service';
import { ConsultasRealtimeService } from '../../core/services/consultas-realtime.service';
import { SaldoDias } from '../../core/models/politica.model';
import { Solicitud } from '../../models/solicitud.model';
import { FormularioComponent } from '../formulario/formulario.component';

@Component({
  selector: 'app-saldo-dashboard',
  standalone: true,
  imports: [DatePipe, FormularioComponent],
  templateUrl: './saldo-dash-board.component.html',
  styleUrls: ['./saldo-dash-board.component.css']
})
export class MiSaldoDashboardComponent implements OnDestroy {
  private politicasSvc = inject(PoliticasService);
  private solicitudesSvc = inject(SolicitudesService);
  private auth = inject(AuthService);
  private router = inject(Router);
  private consultasRealtime = inject(ConsultasRealtimeService);

  saldo = signal<SaldoDias | null>(null);
  solicitudes = signal<Solicitud[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  modalNuevaAbierta = signal(false);
  // Muestra solo las primeras 3 solicitudes para mantener limpia la vista
  solicitudesRecientes = computed(() => this.solicitudes().slice(0, 3));

  constructor() {
    this.cargarDashboard();
    const colaboradorId = this.auth.currentUser()?.id;
    if (colaboradorId) {
      this.consultasRealtime.conectar(colaboradorId);
    }
  }

  ngOnDestroy(): void {
    this.consultasRealtime.desconectar();
  }

  cargarDashboard(): void {
    const userId = this.auth.currentUser()?.id;
    if (!userId) return;

    this.loading.set(true);
    this.error.set(null);

    // Carga de saldo
    this.politicasSvc.obtenerSaldo(userId).subscribe({
      next: (res) => this.saldo.set(res),
      error: () => this.error.set('No se pudo obtener la información de tu saldo de vacaciones.')
    });

    // Carga de solicitudes
    this.solicitudesSvc.listar(userId).subscribe({
      next: (res) => {
        this.solicitudes.set(res);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  abrirModalNueva(): void {
    this.modalNuevaAbierta.set(true);
  }

  cerrarModalNueva(): void {
    this.modalNuevaAbierta.set(false);
  }

  onSolicitudCreada(): void {
    this.cerrarModalNueva();
    this.cargarDashboard();
  }

  irAHistorialCompleto(): void {
    this.router.navigate(['/solicitudes']);
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