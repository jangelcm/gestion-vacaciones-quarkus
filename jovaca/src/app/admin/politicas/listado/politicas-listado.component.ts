import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PoliticasService } from '../../../core/services/politicas.service';
import { UsuariosService } from '../../../core/services/usuarios.service';
import { Politica, SaldoDias } from '../../../core/models/politica.model';
import { Usuario } from '../../../core/models/usuario.model';
import { ModalComponent } from '../../../shared/modal/modal.component';
import { PoliticaFormComponent } from '../politica-form/politica-form.component';
import { AsignarSaldoFormComponent } from '../asignar-saldo/asignar-saldo-form.component';

@Component({
    selector: 'app-politicas-listado',
    standalone: true,
    imports: [FormsModule, ModalComponent, PoliticaFormComponent, AsignarSaldoFormComponent],
    templateUrl: './politicas-listado.component.html',
    styleUrl: './politicas-listado.component.css'
})
export class PoliticasListadoComponent implements OnInit {
    private svc = inject(PoliticasService);
    private usuariosSvc = inject(UsuariosService);

    politicas = signal<Politica[]>([]);
    loading = signal(false);
    error = signal<string | null>(null);

    modalPolitica = signal(false);
    politicaEditando = signal<Politica | null>(null);

    usuarios = signal<Usuario[]>([]);
    colaboradorConsultado: number | null = null;
    saldo = signal<SaldoDias | null>(null);
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
        this.svc.listar().subscribe({
            next: (data) => { this.politicas.set(data); this.loading.set(false); },
            error: () => { this.error.set('No se pudieron cargar las políticas'); this.loading.set(false); }
        });
    }

    nombreUsuario(id: number): string {
        return this.usuarios().find(u => u.id === id)?.username ?? `#${id}`;
    }

    abrirCrear(): void {
        this.politicaEditando.set(null);
        this.modalPolitica.set(true);
    }

    abrirEditar(p: Politica): void {
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

    eliminar(p: Politica): void {
        if (!confirm(`¿Eliminar la política "${p.nombre}"? Esta acción no se puede deshacer.`)) {
            return;
        }
        this.svc.eliminar(p.id).subscribe({
            next: () => this.cargar(),
            error: () => this.error.set('No se pudo eliminar la política')
        });
    }

    consultarSaldo(): void {
        if (!this.colaboradorConsultado) return;
        this.saldoLoading.set(true);
        this.saldoError.set(null);
        this.saldo.set(null);
        this.svc.obtenerSaldo(this.colaboradorConsultado).subscribe({
            next: (s) => { this.saldo.set(s); this.saldoLoading.set(false); },
            error: () => {
                this.saldoError.set('Este colaborador no tiene una política asignada');
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
