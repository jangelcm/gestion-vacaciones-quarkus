import { Component, EventEmitter, Output, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { SolicitudesService } from '../../services/solicitudes.service';
import { AuthService } from '../../core/services/auth.service';
import { extraerMensajeError } from '../../core/utils/error.util';

function fechaFinValidator(control: AbstractControl): ValidationErrors | null {
    const inicio = control.parent?.get('fechaInicio')?.value;
    const fin = control.value;
    if (inicio && fin && fin <= inicio) {
        return { fechaFinInvalida: true };
    }
    return null;
}

@Component({
    selector: 'app-formulario',
    standalone: true,
    imports: [ReactiveFormsModule],
    templateUrl: './formulario.component.html',
    styleUrl: './formulario.component.css'
})
export class FormularioComponent {
    private fb = inject(FormBuilder);
    private svc = inject(SolicitudesService);
    private auth = inject(AuthService);

    @Output() creada = new EventEmitter<void>();
    @Output() cancelar = new EventEmitter<void>();

    loading = signal(false);
    error = signal<string | null>(null);
    exito = signal(false);

    form = this.fb.group({
        fechaInicio: ['', Validators.required],
        fechaFin: ['', [Validators.required, fechaFinValidator]]
    });

    get f() { return this.form.controls; }

    onFechaInicioChange(): void {
        this.f.fechaFin.updateValueAndValidity();
    }

    enviar(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }

        const colaboradorId = this.auth.currentUser()?.id;
        if (!colaboradorId) {
            this.error.set('No se pudo determinar el colaborador de la sesión actual');
            return;
        }

        this.loading.set(true);
        this.error.set(null);
        const { fechaInicio, fechaFin } = this.form.value;
        this.svc.crear({ colaboradorId, fechaInicio: fechaInicio!, fechaFin: fechaFin! }).subscribe({
            next: () => {
                this.loading.set(false);
                this.exito.set(true);
                setTimeout(() => this.creada.emit(), 1200);
            },
            error: (err) => { this.error.set(extraerMensajeError(err, 'Error al enviar la solicitud. Intente nuevamente.')); this.loading.set(false); }
        });
    }
}
