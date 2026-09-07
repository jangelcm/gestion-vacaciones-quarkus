import { Component, EventEmitter, inject, OnInit, Output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UsuariosService } from '../../../core/services/usuarios.service';
import { Rol } from '../../../core/models/rol.model';

@Component({
    selector: 'app-crear-usuario',
    standalone: true,
    imports: [ReactiveFormsModule],
    templateUrl: './crear-usuario.component.html',
    styleUrl: './crear-usuario.component.css'
})
export class CrearUsuarioComponent implements OnInit {
    private fb = inject(FormBuilder);
    private svc = inject(UsuariosService);

    @Output() usuarioCreado = new EventEmitter<void>();
    @Output() cancelar = new EventEmitter<void>();

    roles = signal<Rol[]>([]);
    loading = signal(false);
    error = signal<string | null>(null);
    exito = signal<string | null>(null);

    form = this.fb.group({
        username: ['', Validators.required],
        password: ['', [Validators.required, Validators.minLength(6)]],
        rol: ['', Validators.required]
    });

    get f() { return this.form.controls; }

    ngOnInit(): void {
        this.svc.listarRoles().subscribe({
            next: (roles) => this.roles.set(roles),
            error: () => this.error.set('No se pudieron cargar los roles disponibles')
        });
    }

    crear(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.loading.set(true);
        this.error.set(null);
        this.exito.set(null);

        const { username, password, rol } = this.form.value;
        this.svc.crear({ username: username!, password: password!, rol: rol! }).subscribe({
            next: (usuario) => {
                this.loading.set(false);
                this.exito.set(`Usuario "${usuario.username}" creado correctamente con rol "${rol}"`);
                this.form.reset();
                setTimeout(() => this.usuarioCreado.emit(), 1200);
            },
            error: (err) => {
                this.loading.set(false);
                if (err.status === 409) {
                    this.error.set('Ese usuario ya existe');
                } else if (err.status === 400) {
                    this.error.set(typeof err.error === 'string' ? err.error : 'Rol no válido');
                } else {
                    this.error.set('No se pudo crear el usuario. Intente nuevamente.');
                }
            }
        });
    }
}
