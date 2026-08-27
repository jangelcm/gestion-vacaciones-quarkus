import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Solicitud, SolicitudPayload } from '../models/solicitud.model';

@Injectable({ providedIn: 'root' })
export class SolicitudesService {
    private readonly BASE = 'http://localhost:8080/api/v1/solicitudes';

    constructor(private http: HttpClient) { }

    crear(payload: SolicitudPayload): Observable<Solicitud> {
        return this.http.post<Solicitud>(`${this.BASE}/`, payload);
    }

    listar(colaboradorId: number): Observable<Solicitud[]> {
        return this.http.get<Solicitud[]>(`${this.BASE}/usuario/${colaboradorId}`);
    }

    detalle(id: number): Observable<Solicitud> {
        return this.http.get<Solicitud>(`${this.BASE}/${id}`);
    }
}
