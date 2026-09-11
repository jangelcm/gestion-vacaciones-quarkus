import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PoliticasService } from '../../../core/services/politicas.service';
import { ConsultasService } from '../../../core/services/consultas.service';
import { UsuariosService } from '../../../core/services/usuarios.service';
import { Politica } from '../../../core/models/politica.model';
import { BalanceVacacionalDto, PoliticaConsultaDto } from '../../../core/models/consulta.model';
import { Usuario } from '../../../core/models/usuario.model';
import { ModalComponent } from '../../../shared/modal/modal.component';
import { PoliticaFormComponent } from '../politica-form/politica-form.component';
import { AsignarSaldoFormComponent } from '../asignar-saldo/asignar-saldo-form.component';
import { extraerMensajeError } from '../../../core/utils/error.util';

@Component({
    selector: 'app-politicas-listado',
    standalone: true,
    imports: [FormsModule, ModalComponent, PoliticaFormComponent, AsignarSaldoFormComponent],
    templateUrl: './politicas-listado.component.html',
    styleUrl: './politicas-listado.component.css'
})
export class PoliticasListadoComponent implements OnInit {
    private svc = inject(PoliticasService);
    private consultasSvc = inject(ConsultasService);
    private usuariosSvc = inject(UsuariosService);

    politicas = signal<PoliticaConsultaDto[]>([]);
    loading = signal(false);
    error = signal<string | null>(null);

    modalPolitica = signal(false);
    politicaEditando = signal<Politica | null>(null);

    usuarios = signal<Usuario[]>([]);
    colaboradorConsultado: number | null = null;
    saldo = signal<BalanceVacacionalDto | null>(null);
    saldoLoading = signal(false);
    saldoError = signal<string | null>(null);

    modalAsignar = signal(false);

    ngOnInit(): void {
        this.cargar();
        this.usuariosSvc.listarUsuarios().subscribe({
            next: (res) => this.usuarios.set(res.content)
        });
    }

    cargar(): void {
        this.loading.set(true);
        this.error.set(null);
        // Lado de lectura CQRS: ms-consultas, no ms-politicas directo.
        this.consultasSvc.listarPoliticas().subscribe({
            next: (data) => { this.politicas.set(data); this.loading.set(false); },
            error: (err) => { this.error.set(extraerMensajeError(err, 'No se pudieron cargar las políticas')); this.loading.set(false); }
        });
    }

    nombreUsuario(id: number): string {
        return this.usuarios().find(u => u.id === id)?.username ?? `#${id}`;
    }

    abrirCrear(): void {
        this.politicaEditando.set(null);
        this.modalPolitica.set(true);
    }

    abrirEditar(p: PoliticaConsultaDto): void {
        this.politicaEditando.set(p);
        this.modalPolitica.set(true);
    }

    cerrarModalPolitica(): void {
        this.modalPolitica.set(false);
        this.politicaEditando.set(null);
    }

    onPoliticaGuardada(): void {
        this.cerrarModalPolitica();
        this.cargar();
    }

    desactivar(p: PoliticaConsultaDto): void {
        if (!confirm(`¿Desactivar la política "${p.nombre}"?`)) {
            return;
        }
        this.svc.desactivar(p.id).subscribe({
            next: () => this.cargar(),
            error: (err) => this.error.set(extraerMensajeError(err, 'No se pudo desactivar la política'))
        });
    }

    consultarSaldo(): void {
        if (!this.colaboradorConsultado) return;
        this.saldoLoading.set(true);
        this.saldoError.set(null);
        this.saldo.set(null);
        // Lado de lectura CQRS: ms-consultas, no ms-politicas directo.
        this.consultasSvc.obtenerBalanceColaborador(this.colaboradorConsultado).subscribe({
            next: (s) => { this.saldo.set(s); this.saldoLoading.set(false); },
            error: (err) => {
                this.saldoError.set(extraerMensajeError(err, 'Este colaborador no tiene una política asignada'));
                this.saldoLoading.set(false);
            }
        });
    }

    abrirAsignar(): void {
        this.modalAsignar.set(true);
    }

    cerrarModalAsignar(): void {
        this.modalAsignar.set(false);
    }

    onAsignado(): void {
        this.cerrarModalAsignar();
        if (this.colaboradorConsultado) {
            this.consultarSaldo();
        }
    }
}
