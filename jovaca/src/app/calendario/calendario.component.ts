import { Component, computed, inject } from '@angular/core';
import { AuthService } from '../core/services/auth.service';
import { CalendarioMensualComponent } from './mensual/calendario-mensual.component';
import { CalendarioEquipoComponent } from './equipo/calendario-equipo.component';

@Component({
    selector: 'app-calendario',
    standalone: true,
    imports: [CalendarioMensualComponent, CalendarioEquipoComponent],
    template: `
        @if (esVistaEquipo()) {
            <app-calendario-equipo />
        } @else {
            <app-calendario-mensual />
        }
    `
})
export class CalendarioComponent {
    private auth = inject(AuthService);

    esVistaEquipo = computed(() =>
        this.auth.hasRole('Jefe Inmediato') ||
        this.auth.hasRole('Recursos Humanos') ||
        this.auth.hasRole('Administrador')
    );
}
