import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CrearUsuarioPayload, Rol, UsuarioCreado } from '../models/rol.model';
import { PageResponse, Usuario } from '../models/usuario.model';

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
}
