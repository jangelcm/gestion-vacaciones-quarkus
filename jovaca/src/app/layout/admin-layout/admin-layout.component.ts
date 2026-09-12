import { Component, OnDestroy, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ConsultasRealtimeService } from '../../core/services/consultas-realtime.service';
import { NotificacionesBellComponent } from '../../shared/notificaciones-bell/notificaciones-bell.component';

@Component({
    selector: 'app-admin-layout',
    standalone: true,
    imports: [RouterLink, RouterLinkActive, RouterOutlet, NotificacionesBellComponent],
    templateUrl: './admin-layout.component.html',
    styleUrl: './admin-layout.component.css'
})
export class AdminLayoutComponent implements OnDestroy {
    auth = inject(AuthService);
    private router = inject(Router);
    private consultasRealtime = inject(ConsultasRealtimeService);

    constructor() {
        // Conexion unica para toda la sesion (el layout persiste mientras navegas entre
        // pantallas): antes cada pantalla (dashboard, historial) abria/cerraba su propio
        // socket al montarse/desmontarse, reabriendo una conexion identica en cada navegacion.
        const colaboradorId = this.auth.currentUser()?.id;
        if (colaboradorId) {
            this.consultasRealtime.conectar(colaboradorId);
        }
    }

    ngOnDestroy(): void {
        this.consultasRealtime.desconectar();
    }

    cerrarSesion(): void {
        this.auth.logout();
        this.router.navigate(['/login']);
    }
}
