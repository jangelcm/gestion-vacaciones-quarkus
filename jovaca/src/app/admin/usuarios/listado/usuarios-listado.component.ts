import { Component, inject, OnInit, signal } from '@angular/core';
import { ModalComponent } from '../../../shared/modal/modal.component';
import { CrearUsuarioComponent } from '../crear-usuario/crear-usuario.component';
import { UsuariosService } from '../../../core/services/usuarios.service';
import { Usuario } from '../../../core/models/usuario.model';

@Component({
    selector: 'app-usuarios-listado',
    standalone: true,
    imports: [ModalComponent, CrearUsuarioComponent],
    templateUrl: './usuarios-listado.component.html',
    styleUrl: './usuarios-listado.component.css'
})
export class UsuariosListadoComponent implements OnInit {
    private svc = inject(UsuariosService);

    usuarios = signal<Usuario[]>([]);
    loading = signal(false);
    error = signal<string | null>(null);
    modalAbierto = signal(false);

    ngOnInit(): void {
        this.cargar();
    }

    cargar(): void {
        this.loading.set(true);
        this.error.set(null);
        this.svc.listarUsuarios().subscribe({
            next: (res) => { this.usuarios.set(res.content); this.loading.set(false); },
            error: () => { this.error.set('No se pudo cargar la lista de usuarios'); this.loading.set(false); }
        });
    }

    abrirModal(): void {
        this.modalAbierto.set(true);
    }

    cerrarModal(): void {
        this.modalAbierto.set(false);
    }

    onUsuarioCreado(): void {
        this.modalAbierto.set(false);
        this.cargar();
    }
}
