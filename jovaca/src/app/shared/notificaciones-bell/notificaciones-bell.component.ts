import { Component, ElementRef, HostListener, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { NotificacionesService } from '../../core/services/notificaciones.service';

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

    badgeClaseEstado(estado: string): string {
        const map: Record<string, string> = {
            ENVIADO: 'badge-enviado',
            PENDIENTE: 'badge-pendiente',
            FALLIDO: 'badge-fallido'
        };
        return map[estado] ?? '';
    }
}
