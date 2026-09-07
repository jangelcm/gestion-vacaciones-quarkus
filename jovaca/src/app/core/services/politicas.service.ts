import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AsignarPoliticaPayload, Politica, PoliticaPayload, SaldoDias } from '../models/politica.model';

@Injectable({ providedIn: 'root' })
export class PoliticasService {
    private readonly BASE = `${environment.apiBaseUrl}/api/v1/politicas`;

    constructor(private http: HttpClient) { }

    listar(): Observable<Politica[]> {
        return this.http.get<Politica[]>(this.BASE);
    }

    crear(payload: PoliticaPayload): Observable<Politica> {
        return this.http.post<Politica>(this.BASE, payload);
    }

    actualizar(id: number, payload: PoliticaPayload): Observable<Politica> {
        return this.http.put<Politica>(`${this.BASE}/${id}`, payload);
    }

    eliminar(id: number): Observable<Politica> {
        return this.http.delete<Politica>(`${this.BASE}/${id}`);
    }

    obtenerSaldo(colaboradorId: number): Observable<SaldoDias> {
        return this.http.get<SaldoDias>(`${this.BASE}/saldo/${colaboradorId}`);
    }

    asignar(politicaId: number, colaboradorId: number, payload: AsignarPoliticaPayload): Observable<void> {
        return this.http.post<void>(`${this.BASE}/${politicaId}/colaboradores/${colaboradorId}`, payload);
    }
}
