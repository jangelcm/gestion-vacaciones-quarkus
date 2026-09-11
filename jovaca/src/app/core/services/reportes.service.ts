import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ReporteResumen {
    pendientes: number;
    aprobadas: number;
    rechazadas: number;
    canceladas: number;
    total: number;
}

export interface SaldoReporteDto {
    colaboradorId: number;
    politicaId: number | null;
    politicaNombre: string | null;
    diasDisponibles: number;
    diasGozados: number;
    diasHabilitados: number;
    saldoActual: number;
    diasAcumulados: number;
    diasPendientes: number;
}

@Injectable({ providedIn: 'root' })
export class ReportesService {
    private readonly BASE = `${environment.apiBaseUrl}/api/v1/reportes`;

    constructor(private http: HttpClient) { }

    obtenerResumen(desde?: string, hasta?: string): Observable<ReporteResumen> {
        let params = new HttpParams();
        if (desde) params = params.set('desde', desde);
        if (hasta) params = params.set('hasta', hasta);
        return this.http.get<ReporteResumen>(`${this.BASE}/resumen`, { params });
    }

    obtenerSaldos(politicaId?: number): Observable<SaldoReporteDto[]> {
        let params = new HttpParams();
        if (politicaId) params = params.set('politicaId', politicaId);
        return this.http.get<SaldoReporteDto[]>(`${this.BASE}/saldos`, { params });
    }
}
