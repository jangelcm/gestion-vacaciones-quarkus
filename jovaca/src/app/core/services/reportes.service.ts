import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ReporteResumen {
    pendientes: number;
    aprobadas: number;
    rechazadas: number;
    canceladas: number;
    mensaje: string;
}

@Injectable({ providedIn: 'root' })
export class ReportesService {
    private readonly BASE = `${environment.apiBaseUrl}/api/v1/reportes`;

    constructor(private http: HttpClient) { }

    obtenerResumen(): Observable<ReporteResumen> {
        return this.http.get<ReporteResumen>(`${this.BASE}/resumen`);
    }
}
