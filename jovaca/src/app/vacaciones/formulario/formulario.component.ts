import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { SolicitudesService } from '../../services/solicitudes.service';

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
    private router = inject(Router);

    loading = signal(false);
    error = signal<string | null>(null);
    exito = signal(false);

    form = this.fb.group({
        colaboradorId: [null as number | null, [Validators.required, Validators.min(1)]],
        fechaInicio: ['', Validators.required],
        fechaFin: ['', [Validators.required, fechaFinValidator]]
    });

    get f() { return this.form.controls; }

    onFechaInicioChange(): void {
        this.f.fechaFin.updateValueAndValidity();
    }

    enviar(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.loading.set(true);
        this.error.set(null);
        const { colaboradorId, fechaInicio, fechaFin } = this.form.value;
        this.svc.crear({ colaboradorId: colaboradorId!, fechaInicio: fechaInicio!, fechaFin: fechaFin! }).subscribe({
            next: () => {
                this.loading.set(false);
                this.exito.set(true);
                setTimeout(() => this.router.navigate(['/solicitudes']), 1800);
            },
            error: () => { this.error.set('Error al enviar la solicitud. Intente nuevamente.'); this.loading.set(false); }
        });
    }

    cancelar(): void {
        this.router.navigate(['/solicitudes']);
    }
}
