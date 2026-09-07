export interface Rol {
    idRol: number;
    descripcion: string;
}

export interface CrearUsuarioPayload {
    username: string;
    password: string;
    rol: string;
}

export interface UsuarioCreado {
    id: number;
    username: string;
    isActive: boolean;
    email: string | null;
    telefono: string | null;
}
