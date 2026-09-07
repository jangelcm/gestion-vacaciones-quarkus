import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { NotificacionesBellComponent } from '../../shared/notificaciones-bell/notificaciones-bell.component';

@Component({
    selector: 'app-admin-layout',
    standalone: true,
    imports: [RouterLink, RouterLinkActive, RouterOutlet, NotificacionesBellComponent],
    templateUrl: './admin-layout.component.html',
    styleUrl: './admin-layout.component.css'
})
export class AdminLayoutComponent {
    auth = inject(AuthService);
    private router = inject(Router);

    cerrarSesion(): void {
        this.auth.logout();
        this.router.navigate(['/login']);
    }
}
