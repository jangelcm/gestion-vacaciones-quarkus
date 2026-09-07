import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Solicitud, SolicitudPayload } from '../models/solicitud.model';

@Injectable({ providedIn: 'root' })
export class SolicitudesService {
    private readonly BASE = `${environment.apiBaseUrl}/api/v1/solicitudes`;

    constructor(private http: HttpClient) { }

    crear(payload: SolicitudPayload): Observable<Solicitud> {
        return this.http.post<Solicitud>(`${this.BASE}/`, payload);
    }

    listar(colaboradorId: number): Observable<Solicitud[]> {
        return this.http.get<Solicitud[]>(`${this.BASE}/usuario/${colaboradorId}`);
    }

    listarTodas(): Observable<Solicitud[]> {
        return this.http.get<Solicitud[]>(this.BASE);
    }

    detalle(id: number): Observable<Solicitud> {
        return this.http.get<Solicitud>(`${this.BASE}/${id}`);
    }
}
