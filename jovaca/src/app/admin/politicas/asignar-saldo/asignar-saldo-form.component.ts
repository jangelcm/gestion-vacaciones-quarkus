import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PoliticasService } from '../../../core/services/politicas.service';
import { UsuariosService } from '../../../core/services/usuarios.service';
import { Politica } from '../../../core/models/politica.model';
import { Usuario } from '../../../core/models/usuario.model';

@Component({
    selector: 'app-asignar-saldo-form',
    standalone: true,
    imports: [ReactiveFormsModule],
    templateUrl: './asignar-saldo-form.component.html',
    styleUrl: './asignar-saldo-form.component.css'
})
export class AsignarSaldoFormComponent implements OnInit {
    private fb = inject(FormBuilder);
    private politicasSvc = inject(PoliticasService);
    private usuariosSvc = inject(UsuariosService);

    @Input() politicas: Politica[] = [];

    @Output() asignado = new EventEmitter<void>();
    @Output() cancelar = new EventEmitter<void>();

    usuarios = signal<Usuario[]>([]);
    loading = signal(false);
    error = signal<string | null>(null);

    form = this.fb.group({
        colaboradorId: [null as number | null, Validators.required],
        politicaId: [null as number | null, Validators.required],
        antiguedadMeses: [null as number | null]
    });

    get f() { return this.form.controls; }

    ngOnInit(): void {
        this.usuariosSvc.listarUsuarios().subscribe({
            next: (res) => this.usuarios.set(res.content),
            error: () => this.error.set('No se pudieron cargar los usuarios')
        });
    }

    asignar(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }

        this.loading.set(true);
        this.error.set(null);
        const { colaboradorId, politicaId, antiguedadMeses } = this.form.value;

        this.politicasSvc.asignar(politicaId!, colaboradorId!, { antiguedadMeses: antiguedadMeses ?? null }).subscribe({
            next: () => { this.loading.set(false); this.asignado.emit(); },
            error: (err) => {
                this.loading.set(false);
                if (err.status === 409) {
                    this.error.set('Este colaborador ya tiene una política asignada');
                } else if (err.status === 400) {
                    this.error.set(typeof err.error?.mensaje === 'string' ? err.error.mensaje : 'No cumple los requisitos de la política');
                } else {
                    this.error.set('No se pudo asignar la política. Intente nuevamente.');
                }
            }
        });
    }
}
