# Proyecto: Gestión de Vacaciones (Microservicios)

Este proyecto implementa arquitectura de microservicios con Quarkus, API Gateway, PostgreSQL, Kafka y frontend Angular.

## Estructura Principal

- `ms-solicitud`: gestión de solicitudes.
- `ms-aprobaciones`: gestión de aprobaciones.
- `ms-politicas`: gestión de políticas de vacaciones y saldos de días.
- `mcsv-gateway`: API Gateway y enrutamiento.
- `mcsv-auth`: microservicio de autenticación (preparado para etapa posterior).
- `jovaca`: frontend Angular.

## Requisitos Previos

- Java 17.
- Maven 3.8+.
- Docker Desktop (con Docker Compose).

## Escenario 1: Levantar Servicios con Docker Compose

Desde la raíz del proyecto:

```bash
docker-compose up -d
```

Si quieres reconstruir imágenes:

```bash
docker-compose up -d --build
```

Para ver logs:

```bash
docker-compose logs -f
```

Para detener:

```bash
docker-compose down
```

## Escenario 2: Compilar y Ejecutar Microservicios Localmente

Navega al directorio de cada microservicio y ejecuta:

```bash
mvn clean install
mvn quarkus:dev
```

Ejemplo:

```bash
cd ms-solicitud
mvn clean install
mvn quarkus:dev
```

Repite el mismo flujo para:

- `ms-aprobaciones`
- `ms-politicas`
- `mcsv-gateway`
- `mcsv-auth` (opcional por ahora)

## Acceso a Endpoints

- API Gateway: http://localhost:8080
- Swagger UI del Gateway: http://localhost:8080/q/swagger-ui

Endpoints directos útiles:

- ms-solicitud: http://localhost:8082
- ms-aprobaciones: http://localhost:8083
- ms-politicas: http://localhost:8081
- Kafka UI: http://localhost:8088
- Frontend Angular (jovaca): http://localhost:4200

## Nota de Base de Datos (PostgreSQL)

Si conectas por cliente SQL (DBeaver, DataGrip, etc.), usa:

- Host: `localhost`
- Puerto: `5432`
- Base solicitudes: `db_solicitudes`
- Usuario: `solicitudes_user`
- Contraseña: `solicitudes_pass`

Base de aprobaciones:

- Base: `db_aprobaciones`
- Usuario: `aprobaciones_user`
- Contraseña: `aprobaciones_pass`

Base de políticas:

- Base: `db_politicas`
- Usuario: `politicas_user`
- Contraseña: `politicas_pass`