export interface Politica {
    id: number;
    nombre: string;
    tipoVacacion: string;
    diasBaseAnio: number;
    antiguedadMinimaMeses: number;
    acumulable: boolean;
    maxDiasAcumulables: number | null;
    activa: boolean;
    createdAt?: string;
    updatedAt?: string;
}

export interface PoliticaPayload {
    nombre: string;
    tipoVacacion: string;
    diasBaseAnio: number;
    antiguedadMinimaMeses: number;
    acumulable: boolean;
    maxDiasAcumulables: number | null;
    activa: boolean;
}

export interface SaldoDias {
    id: number;
    colaboradorId: number;
    politicaId: number;
    diasDisponibles: string;
    diasUsados: string;
    diasAcumulados: string;
    diasHabilitados: string;
    saldoActual: string;
    diasTruncos: string;
    diasTrabajados: string;
    diasPendientes: string;
    fechaIngresoColaborador: string | null;
    fechaAsignacionPolitica: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface AsignarPoliticaPayload {
    fechaInicioPolitica: string;
    fechaIngresoColaborador: string | null;
}
