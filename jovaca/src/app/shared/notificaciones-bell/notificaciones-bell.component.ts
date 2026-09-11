import { Component, ElementRef, HostListener, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { NotificacionesService } from '../../core/services/notificaciones.service';
import { Notificacion } from '../../core/models/notificacion.model';

@Component({
    selector: 'app-notificaciones-bell',
    standalone: true,
    imports: [],
    templateUrl: './notificaciones-bell.component.html',
    styleUrl: './notificaciones-bell.component.css'
})
export class NotificacionesBellComponent implements OnInit, OnDestroy {
    private auth = inject(AuthService);
    private elementRef = inject(ElementRef);
    private router = inject(Router);

    notifSvc = inject(NotificacionesService);
    abierto = signal(false);

    ngOnInit(): void {
        const colaboradorId = this.auth.currentUser()?.id;
        if (colaboradorId) {
            this.notifSvc.conectar(colaboradorId);
        }
    }

    ngOnDestroy(): void {
        this.notifSvc.desconectar();
    }

    toggle(): void {
        const nuevoEstado = !this.abierto();
        this.abierto.set(nuevoEstado);
        if (nuevoEstado) {
            this.notifSvc.marcarComoVistas();
        }
    }

    @HostListener('document:click', ['$event'])
    onDocumentClick(event: MouseEvent): void {
        if (this.abierto() && !this.elementRef.nativeElement.contains(event.target)) {
            this.abierto.set(false);
        }
    }

    ver(n: Notificacion): void {
        this.abierto.set(false);
        this.notifSvc.descartar(n.id);
        this.router.navigate(this.notifSvc.rutaDestino(n));
    }

    cerrar(n: Notificacion): void {
        this.notifSvc.descartar(n.id);
    }

    badgeClaseEstado(estado: string): string {
        const map: Record<string, string> = {
            ENVIADO: 'badge-enviado',
            PENDIENTE: 'badge-pendiente',
            FALLIDO: 'badge-fallido'
        };
        return map[estado] ?? '';
    }
}
