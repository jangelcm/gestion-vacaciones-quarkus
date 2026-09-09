import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [ReactiveFormsModule],
    templateUrl: './login.component.html',
    styleUrl: './login.component.css'
})
export class LoginComponent {
    private fb = inject(FormBuilder);
    private auth = inject(AuthService);
    private router = inject(Router);

    loading = signal(false);
    error = signal<string | null>(null);

    form = this.fb.group({
        username: ['', Validators.required],
        password: ['', Validators.required]
    });

    get f() { return this.form.controls; }

    entrar(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.loading.set(true);
        this.error.set(null);

        const { username, password } = this.form.value;
        this.auth.login({ username: username!, password: password! }).subscribe({
            next: () => {
                this.loading.set(false);
                this.router.navigate(['/dashboard']);
            },
            error: (err) => {
                this.loading.set(false);
                this.error.set(err.status === 401
                    ? 'Usuario o contraseña incorrectos'
                    : 'No se pudo iniciar sesión. Intente nuevamente.');
            }
        });
    }
}
