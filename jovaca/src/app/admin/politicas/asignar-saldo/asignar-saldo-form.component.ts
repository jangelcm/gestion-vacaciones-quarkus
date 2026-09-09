import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PoliticasService } from '../../../core/services/politicas.service';
import { UsuariosService } from '../../../core/services/usuarios.service';
import { Politica } from '../../../core/models/politica.model';
import { Usuario } from '../../../core/models/usuario.model';
import { firstValueFrom } from 'rxjs';

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
    exito = signal<string | null>(null);

    form = this.fb.group({
        rol: ['', Validators.required],
        politicaId: [null as number | null, Validators.required],
        fechaInicioPolitica: ['', Validators.required]
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
        this.exito.set(null);
        const { rol, politicaId, fechaInicioPolitica } = this.form.value;

        this.usuariosSvc.listarUsuariosPorRol(rol!).subscribe({
            next: (usuariosRol) => {
                if (usuariosRol.length === 0) {
                    this.loading.set(false);
                    this.error.set('No hay colaboradores con el rol seleccionado');
                    return;
                }

                let completados = 0;
                let errores = 0;
                const tareas = usuariosRol.map((usuario) =>
                    this.politicasSvc.asignar(politicaId!, usuario.id, {
                        fechaInicioPolitica: fechaInicioPolitica!,
                        fechaIngresoColaborador: usuario.fechaIngreso
                    })
                );

                // Ejecuta la asignacion por lote y contabiliza conflictos sin abortar todo el proceso.
                Promise.all(
                    tareas.map((obs) =>
                        firstValueFrom(obs)
                            .then(() => { completados += 1; })
                            .catch(() => { errores += 1; })
                    )
                ).finally(() => {
                    this.loading.set(false);
                    if (completados > 0) {
                        this.exito.set(`Asignación aplicada a ${completados} colaborador(es)`);
                    }
                    if (errores > 0) {
                        this.error.set(`${errores} colaborador(es) no pudieron asignarse (ya tenían política o no cumplen condiciones)`);
                    }
                    if (completados > 0 && errores === 0) {
                        this.asignado.emit();
                    }
                });
            },
            error: () => {
                this.loading.set(false);
                this.error.set('No se pudo obtener el listado por rol. Intente nuevamente.');
            }
        });
    }
}
