import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CrearUsuarioPayload, Rol, UsuarioCreado } from '../models/rol.model';
import { PageResponse, UpdateUsuarioPayload, Usuario } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuariosService {
    private readonly BASE = environment.apiBaseUrl;

    constructor(private http: HttpClient) { }

    listarRoles(): Observable<Rol[]> {
        return this.http.get<Rol[]>(`${this.BASE}/rols`);
    }

    crear(payload: CrearUsuarioPayload): Observable<UsuarioCreado> {
        return this.http.post<UsuarioCreado>(`${this.BASE}/auth/register`, payload);
    }

    listarUsuarios(): Observable<PageResponse<Usuario>> {
        return this.http.post<PageResponse<Usuario>>(`${this.BASE}/users/pagination`, {
            pageNumber: 0,
            rowsPerPage: 50,
            filters: [],
            sorts: []
        });
    }

    listarUsuariosPorRol(rol: string): Observable<Usuario[]> {
        return this.http.get<Usuario[]>(`${this.BASE}/users/rol/${encodeURIComponent(rol)}`);
    }

    listarPorIds(ids: number[]): Observable<Usuario[]> {
        if (ids.length === 0) {
            return of([]);
        }
        return this.http.get<Usuario[]>(`${this.BASE}/users/batch`, {
            params: { ids: ids.join(',') }
        });
    }

    actualizar(id: number, payload: UpdateUsuarioPayload): Observable<Usuario> {
        return this.http.put<Usuario>(`${this.BASE}/users/${id}`, payload);
    }
}
