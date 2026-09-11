import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PoliticasService } from '../../../core/services/politicas.service';
import { Politica } from '../../../core/models/politica.model';
import { extraerMensajeError } from '../../../core/utils/error.util';

@Component({
    selector: 'app-politica-form',
    standalone: true,
    imports: [ReactiveFormsModule],
    templateUrl: './politica-form.component.html',
    styleUrl: './politica-form.component.css'
})
export class PoliticaFormComponent implements OnInit {
    private fb = inject(FormBuilder);
    private svc = inject(PoliticasService);

    @Input() politica: Politica | null = null;
    @Output() guardada = new EventEmitter<void>();
    @Output() cancelar = new EventEmitter<void>();

    loading = signal(false);
    error = signal<string | null>(null);

    form = this.fb.group({
        nombre: ['', Validators.required],
        tipoVacacion: ['ANUAL', Validators.required],
        diasBaseAnio: [15, [Validators.required, Validators.min(1)]],
        antiguedadMinimaMeses: [0, [Validators.required, Validators.min(0)]],
        acumulable: [false],
        maxDiasAcumulables: [null as number | null],
        activa: [true]
    });

    get f() { return this.form.controls; }
    get esEdicion(): boolean { return this.politica !== null; }

    ngOnInit(): void {
        if (this.politica) {
            this.form.patchValue({
                nombre: this.politica.nombre,
                tipoVacacion: this.politica.tipoVacacion,
                diasBaseAnio: this.politica.diasBaseAnio,
                antiguedadMinimaMeses: this.politica.antiguedadMinimaMeses,
                acumulable: this.politica.acumulable,
                maxDiasAcumulables: this.politica.maxDiasAcumulables,
                activa: this.politica.activa
            });
        }
    }

    guardar(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }

        this.loading.set(true);
        this.error.set(null);
        const payload = {
            nombre: this.form.value.nombre!,
            tipoVacacion: this.form.value.tipoVacacion!,
            diasBaseAnio: this.form.value.diasBaseAnio!,
            antiguedadMinimaMeses: this.form.value.antiguedadMinimaMeses!,
            acumulable: this.form.value.acumulable!,
            maxDiasAcumulables: this.form.value.maxDiasAcumulables ?? null,
            activa: this.form.value.activa!
        };

        const obs = this.esEdicion
            ? this.svc.actualizar(this.politica!.id, payload)
            : this.svc.crear(payload);

        obs.subscribe({
            next: () => { this.loading.set(false); this.guardada.emit(); },
            error: (err) => {
                this.loading.set(false);
                this.error.set(extraerMensajeError(err, 'No se pudo guardar la política. Verifique los datos e intente nuevamente.'));
            }
        });
    }
}
