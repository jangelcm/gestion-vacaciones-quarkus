import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SolicitudesService } from '../../services/solicitudes.service';
import { Solicitud } from '../../models/solicitud.model';

@Component({
  selector: 'app-detalle',
  standalone: true,
  imports: [],
  templateUrl: './detalle.component.html',
  styleUrl: './detalle.component.css'
})
export class DetalleComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private svc = inject(SolicitudesService);
  private router = inject(Router);

  solicitud = signal<Solicitud | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.svc.detalle(id).subscribe({
      next: (data) => { this.solicitud.set(data); this.loading.set(false); },
      error: () => { this.error.set('No se pudo cargar el detalle de la solicitud'); this.loading.set(false); }
    });
  }

  badgeClass(estado: string): string {
    const map: Record<string, string> = {
      PENDIENTE: 'badge-pendiente',
      APROBADA: 'badge-aprobada',
      RECHAZADA: 'badge-rechazada'
    };
    return map[estado] ?? '';
  }

  volver(): void {
    this.router.navigate(['/solicitudes']);
  }
}
