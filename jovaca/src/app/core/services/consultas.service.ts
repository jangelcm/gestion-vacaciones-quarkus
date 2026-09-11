import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BalanceVacacionalDto, PoliticaConsultaDto, SolicitudConsultaDto, SolicitudHistorialDto } from '../models/consulta.model';

@Injectable({ providedIn: 'root' })
export class ConsultasService {
    private readonly BASE = `${environment.apiBaseUrl}/api/v1/consultas`;

    constructor(private http: HttpClient) { }

    listarSolicitudesUsuario(colaboradorId: number): Observable<SolicitudConsultaDto[]> {
        return this.http.get<SolicitudConsultaDto[]>(`${this.BASE}/solicitudes/usuario/${colaboradorId}`);
    }

    listarSolicitudesEnRango(desde: string, hasta: string): Observable<SolicitudConsultaDto[]> {
        return this.http.get<SolicitudConsultaDto[]>(`${this.BASE}/solicitudes`, { params: { desde, hasta } });
    }

    obtenerSolicitud(solicitudId: number): Observable<SolicitudConsultaDto> {
        return this.http.get<SolicitudConsultaDto>(`${this.BASE}/solicitudes/${solicitudId}`);
    }

    listarHistorialSolicitud(solicitudId: number): Observable<SolicitudHistorialDto[]> {
        return this.http.get<SolicitudHistorialDto[]>(`${this.BASE}/historial/${solicitudId}`);
    }

    obtenerBalanceColaborador(colaboradorId: number): Observable<BalanceVacacionalDto> {
        return this.http.get<BalanceVacacionalDto>(`${this.BASE}/balances/${colaboradorId}`);
    }

    listarSolicitudesPendientes(): Observable<SolicitudConsultaDto[]> {
        return this.http.get<SolicitudConsultaDto[]>(`${this.BASE}/solicitudes/pendientes`);
    }

    listarPoliticas(): Observable<PoliticaConsultaDto[]> {
        return this.http.get<PoliticaConsultaDto[]>(`${this.BASE}/politicas`);
    }
}
