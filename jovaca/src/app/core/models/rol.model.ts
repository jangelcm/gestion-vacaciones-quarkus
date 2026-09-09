export interface Rol {
    idRol: number;
    descripcion: string;
}

export interface CrearUsuarioPayload {
    username: string;
    password: string;
    email: string;
    rol: string;
    fechaIngreso: string;
}

export interface UsuarioCreado {
    id: number;
    username: string;
    isActive: boolean;
    email: string | null;
    telefono: string | null;
    fechaIngreso: string | null;
}
