export interface Usuario {
    id: number;
    username: string;
    email: string | null;
    telefono: string | null;
    isActive: boolean;
    fechaIngreso: string | null;
}

export interface UpdateUsuarioPayload {
    username?: string;
    email?: string;
    telefono?: string;
    isActive?: boolean;
}

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}
