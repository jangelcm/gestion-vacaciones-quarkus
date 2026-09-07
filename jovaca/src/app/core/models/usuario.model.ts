export interface Usuario {
    id: number;
    username: string;
    email: string | null;
    telefono: string | null;
    isActive: boolean;
}

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}
