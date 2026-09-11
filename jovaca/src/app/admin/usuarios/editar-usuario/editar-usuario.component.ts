import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UsuariosService } from '../../../core/services/usuarios.service';
import { Usuario } from '../../../core/models/usuario.model';
import { extraerMensajeError } from '../../../core/utils/error.util';

@Component({
    selector: 'app-editar-usuario',
    standalone: true,
    imports: [ReactiveFormsModule],
    templateUrl: './editar-usuario.component.html',
    styleUrl: './editar-usuario.component.css'
})
export class EditarUsuarioComponent implements OnInit {
    private fb = inject(FormBuilder);
    private svc = inject(UsuariosService);

    @Input({ required: true }) usuario!: Usuario;
    @Output() guardado = new EventEmitter<void>();
    @Output() cancelar = new EventEmitter<void>();

    loading = signal(false);
    error = signal<string | null>(null);

    form = this.fb.group({
        username: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        telefono: [''],
        isActive: [true]
    });

    get f() { return this.form.controls; }

    ngOnInit(): void {
        this.form.patchValue({
            username: this.usuario.username,
            email: this.usuario.email ?? '',
            telefono: this.usuario.telefono ?? '',
            isActive: this.usuario.isActive
        });
    }

    guardar(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }

        this.loading.set(true);
        this.error.set(null);
        const { username, email, telefono, isActive } = this.form.value;

        this.svc.actualizar(this.usuario.id, {
            username: username!,
            email: email!,
            telefono: telefono || undefined,
            isActive: isActive!
        }).subscribe({
            next: () => { this.loading.set(false); this.guardado.emit(); },
            error: (err) => {
                this.loading.set(false);
                this.error.set(extraerMensajeError(err, 'No se pudo actualizar el usuario. Intente nuevamente.'));
            }
        });
    }
}
