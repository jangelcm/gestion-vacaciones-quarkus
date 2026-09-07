import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Aprobacion, AprobarPayload, RechazarPayload } from '../models/aprobacion.model';

@Injectable({ providedIn: 'root' })
export class AprobacionesService {
    private readonly BASE = `${environment.apiBaseUrl}/api/v1/aprobaciones`;

    constructor(private http: HttpClient) { }

    listarPendientes(): Observable<Aprobacion[]> {
        return this.http.get<Aprobacion[]>(this.BASE);
    }

    aprobar(solicitudId: number, payload: AprobarPayload): Observable<Aprobacion> {
        return this.http.post<Aprobacion>(`${this.BASE}/${solicitudId}/aprobar`, payload);
    }

    rechazar(solicitudId: number, payload: RechazarPayload): Observable<Aprobacion> {
        return this.http.post<Aprobacion>(`${this.BASE}/${solicitudId}/rechazar`, payload);
    }
}
