export interface LoginRequest {
    username: string;
    password: string;
}

export interface LoginResponse {
    access_token: string;
    refresh_token: string;
}

export interface JwtPayload {
    sub: string;
    groups: string[];
    userId: number;
    exp: number;
    iat: number;
}

export interface CurrentUser {
    id: number;
    username: string;
    roles: string[];
}
