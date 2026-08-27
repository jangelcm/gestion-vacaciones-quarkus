CREATE TABLE politica (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo_vacacion VARCHAR(30) NOT NULL,
    dias_base_anio INTEGER NOT NULL CHECK (dias_base_anio > 0),
    antiguedad_minima_meses INTEGER NOT NULL DEFAULT 0,
    acumulable BOOLEAN NOT NULL DEFAULT false,
    max_dias_acumulables INTEGER,
    activa BOOLEAN NOT NULL DEFAULT true,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT now(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE regla_especial (
    id BIGSERIAL PRIMARY KEY,
    politica_id BIGINT NOT NULL REFERENCES politica(id),
    condicion VARCHAR(255) NOT NULL,
    dias_adicionales INTEGER NOT NULL DEFAULT 0,
    descripcion VARCHAR(255),
    activa BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE saldo_dias (
    id BIGSERIAL PRIMARY KEY,
    colaborador_id BIGINT NOT NULL UNIQUE,
    politica_id BIGINT NOT NULL REFERENCES politica(id),
    dias_disponibles NUMERIC(5,1) NOT NULL DEFAULT 0 CHECK (dias_disponibles >= 0),
    dias_usados NUMERIC(5,1) NOT NULL DEFAULT 0,
    dias_acumulados NUMERIC(5,1) NOT NULL DEFAULT 0,
    ultima_actualizacion TIMESTAMP NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE movimiento_saldo (
    id BIGSERIAL PRIMARY KEY,
    saldo_id BIGINT NOT NULL REFERENCES saldo_dias(id),
    solicitud_id BIGINT NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    dias NUMERIC(5,1) NOT NULL,
    evento_origen VARCHAR(50) NOT NULL,
    evento_id VARCHAR(100) NOT NULL UNIQUE,
    fecha TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_saldo_colaborador ON saldo_dias(colaborador_id);
CREATE INDEX idx_movimiento_saldo_id ON movimiento_saldo(saldo_id);
CREATE INDEX idx_movimiento_solicitud ON movimiento_saldo(solicitud_id);
CREATE INDEX idx_politica_activa ON politica(activa) WHERE activa = true;