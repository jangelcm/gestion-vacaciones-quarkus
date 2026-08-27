-- Datos de prueba para politicas-service
-- Ejecutar manualmente en PostgreSQL si no usas el DataSeeder automatico.
-- Columnas alineadas con las entidades JPA (created_at / updated_at).

BEGIN;

DELETE FROM movimiento_saldo;
DELETE FROM saldo_dias;
DELETE FROM regla_especial;
DELETE FROM politica;

INSERT INTO politica (
    id, nombre, tipo_vacacion, dias_base_anio, antiguedad_minima_meses,
    acumulable, max_dias_acumulables, activa, created_at, updated_at
) VALUES
(1, 'Vacaciones anuales', 'ANUAL', 15, 0, TRUE, 30, TRUE, NOW(), NOW()),
(2, 'Vacaciones premium', 'ANUAL', 20, 12, TRUE, 40, TRUE, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('politica', 'id'), (SELECT MAX(id) FROM politica));

INSERT INTO regla_especial (
    id, politica_id, condicion, dias_adicionales, descripcion, activa, created_at, updated_at
) VALUES
(1, 1, 'ANTIGUEDAD>=60', 3, '3 dias adicionales por antiguedad >= 60 meses', TRUE, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('regla_especial', 'id'), (SELECT MAX(id) FROM regla_especial));

INSERT INTO saldo_dias (
    id, colaborador_id, politica_id, dias_disponibles, dias_usados, dias_acumulados,
    created_at, updated_at, version
) VALUES
(1, 1001, 1, 10.0, 2.0, 1.0, NOW(), NOW(), 0),
(2, 1002, 1,  2.0, 8.0, 0.0, NOW(), NOW(), 0),
(3, 1003, 1,  5.0, 0.0, 0.0, NOW(), NOW(), 0),
(4, 1004, 2, 15.0, 0.0, 0.0, NOW(), NOW(), 0);

SELECT setval(pg_get_serial_sequence('saldo_dias', 'id'), (SELECT MAX(id) FROM saldo_dias));

COMMIT;
