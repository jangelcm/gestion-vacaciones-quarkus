import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CurrentUser, JwtPayload, LoginRequest, LoginResponse } from '../models/auth.model';

const TOKEN_KEY = 'jovaca_access_token';
const REFRESH_KEY = 'jovaca_refresh_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly BASE = `${environment.apiBaseUrl}/auth`;

    private readonly currentUserSignal = signal<CurrentUser | null>(this.readUserFromStorage());

    readonly currentUser = this.currentUserSignal.asReadonly();
    readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

    constructor(private http: HttpClient) { }

    login(payload: LoginRequest): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(`${this.BASE}/login`, payload).pipe(
            tap((res) => {
                localStorage.setItem(TOKEN_KEY, res.access_token);
                localStorage.setItem(REFRESH_KEY, res.refresh_token);
                this.currentUserSignal.set(this.buildCurrentUser(res.access_token));
            })
        );
    }

    logout(): void {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(REFRESH_KEY);
        this.currentUserSignal.set(null);
    }

    getToken(): string | null {
        return localStorage.getItem(TOKEN_KEY);
    }

    hasRole(role: string): boolean {
        return this.currentUserSignal()?.roles.includes(role) ?? false;
    }

    private readUserFromStorage(): CurrentUser | null {
        const token = localStorage.getItem(TOKEN_KEY);
        if (!token) {
            return null;
        }
        return this.buildCurrentUser(token);
    }

    private buildCurrentUser(token: string): CurrentUser | null {
        const payload = this.decodeToken(token);
        if (!payload || this.isExpired(payload)) {
            this.logout();
            return null;
        }
        return { id: payload.userId, username: payload.sub, roles: payload.groups ?? [] };
    }

    private isExpired(payload: JwtPayload): boolean {
        return payload.exp * 1000 <= Date.now();
    }

    private decodeToken(token: string): JwtPayload | null {
        try {
            const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
            return JSON.parse(atob(base64));
        } catch {
            return null;
        }
    }
}
