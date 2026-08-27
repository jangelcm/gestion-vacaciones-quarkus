# Especificación Técnica — `ms-aprobaciones`

## Resumen

Microservicio de **lado comando (CQRS)** encargado de gestionar el flujo de aprobación/rechazo de solicitudes de vacaciones.  
Persiste en **PostgreSQL** y se comunica vía **Apache Kafka** con el resto del sistema.

---

## Modelo de Dominio

### `Aprobacion` (entidad de dominio)

| Campo              | Tipo                  | Descripción                                            |
|--------------------|-----------------------|--------------------------------------------------------|
| `id`               | `Long`                | PK auto-generada                                       |
| `solicitudId`      | `Long`                | ID de la solicitud (referencia lógica a ms-solicitudes)|
| `aprobadorId`      | `String`              | ID del usuario que aprueba/rechaza                     |
| `estado`           | `EstadoAprobacion`    | PENDIENTE, APROBADO, RECHAZADO                         |
| `comentario`       | `String`              | Observación del aprobador (nullable)                   |
| `fechaAprobacion`  | `LocalDateTime`       | Timestamp de la decisión                               |
| `nivelAprobacion`  | `int`                 | Nivel del flujo multi-nivel (1 = jefe inmediato, etc.) |

### `EstadoAprobacion` (enum)

```
PENDIENTE → APROBADO
         → RECHAZADO
```

### Métodos de dominio en `Aprobacion`

- `static Aprobacion nuevaPendiente(Long solicitudId)` — crea una aprobación en estado PENDIENTE
- `void aprobar(String aprobadorId, String comentario)` — transiciona a APROBADO
- `void rechazar(String aprobadorId, String motivo)` — transiciona a RECHAZADO

---

## Puertos de Entrada (`domain/ports/in/`)

### `AprobarSolicitudUseCase`

```java
Aprobacion registrarParaAprobacion(Long solicitudId);
Aprobacion aprobar(Long solicitudId, String aprobadorId, String comentario);
```

### `RechazarSolicitudUseCase`

```java
Aprobacion rechazar(Long solicitudId, String aprobadorId, String motivo);
```

---

## Puertos de Salida (`domain/ports/out/`)

### `AprobacionRepositoryPort`

```java
Aprobacion guardar(Aprobacion aprobacion);
Optional<Aprobacion> buscarPorSolicitudId(Long solicitudId);
Optional<Aprobacion> buscarPorId(Long id);
List<Aprobacion> listarPendientes();
```

### `AprobacionEventPublisherPort`

```java
void publicarSolicitudAprobada(Long solicitudId, String aprobadorId, String comentario);
void publicarSolicitudRechazada(Long solicitudId, String aprobadorId, String motivo);
```

---

## Flujo Multi-nivel de Aprobación

```
1. [Kafka] solicitud.creada recibida
      → crear Aprobacion(estado=PENDIENTE, nivel=1)

2. [REST] Jefe Inmediato llama POST /api/v1/aprobaciones/{id}/aprobar
      → Aprobacion.aprobar(aprobadorId, comentario)
      → Kafka: publicar solicitud.aprobada

   OR

   [REST] Jefe Inmediato llama POST /api/v1/aprobaciones/{id}/rechazar
      → Aprobacion.rechazar(aprobadorId, motivo)
      → Kafka: publicar solicitud.rechazada
```

> **Nota:** El flujo multi-nivel (nivel 2 = RRHH, nivel 3 = gerencia) puede extenderse  
> añadiendo más niveles al campo `nivelAprobacion` y usando estados intermedios.

---

## Eventos Kafka

| Dirección   | Topic                  | Canal SmallRye              | Payload                  |
|-------------|------------------------|-----------------------------|--------------------------|
| Consumidor  | `solicitud.creada`     | `solicitud-creada`          | `SolicitudCreadaEventDTO`|
| Productor   | `solicitud.aprobada`   | `solicitud-aprobada`        | `AprobacionEventoDTO`    |
| Productor   | `solicitud.rechazada`  | `solicitud-rechazada`       | `AprobacionEventoDTO`    |

### `SolicitudCreadaEventDTO` (payload de entrada)

```json
{
  "id": 42,
  "colaboradorId": "EMP-001",
  "fechaInicio": "2026-09-01",
  "fechaFin": "2026-09-15",
  "estado": "PENDIENTE"
}
```

### `AprobacionEventoDTO` (payload de salida)

```json
{
  "solicitudId": 42,
  "aprobadorId": "MGR-007",
  "estado": "APROBADO",
  "comentario": "Aprobado sin observaciones"
}
```

---

## Persistencia SQL (PostgreSQL)

### Tabla `aprobaciones`

```sql
CREATE TABLE aprobaciones (
    id                BIGSERIAL PRIMARY KEY,
    solicitud_id      BIGINT NOT NULL UNIQUE,
    aprobador_id      VARCHAR(100),
    estado            VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    comentario        TEXT,
    fecha_aprobacion  TIMESTAMP,
    nivel_aprobacion  INTEGER NOT NULL DEFAULT 1
);
```

> Hibernate genera el DDL automáticamente con `quarkus.hibernate-orm.database.generation=update`.

---

## Endpoints REST

| Método | Ruta                                        | Descripción                        |
|--------|---------------------------------------------|------------------------------------|
| `GET`  | `/api/v1/aprobaciones`                      | Listar aprobaciones pendientes     |
| `GET`  | `/api/v1/aprobaciones/{id}`                 | Obtener aprobación por ID          |
| `POST` | `/api/v1/aprobaciones/{solicitudId}/aprobar`| Aprobar una solicitud              |
| `POST` | `/api/v1/aprobaciones/{solicitudId}/rechazar`| Rechazar una solicitud            |

### Request body para aprobar

```json
{ "aprobadorId": "MGR-007", "comentario": "Sin observaciones" }
```

### Request body para rechazar

```json
{ "aprobadorId": "MGR-007", "motivo": "Período de alta demanda" }
```

---

## Configuración (`application.properties`)

```properties
# PostgreSQL
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${DB_USER:aprobaciones_user}
quarkus.datasource.password=${DB_PASS:secret}
quarkus.datasource.jdbc.url=jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:aprobaciones_db}
quarkus.hibernate-orm.database.generation=update

# Kafka
kafka.bootstrap.servers=${KAFKA_BOOTSTRAP:localhost:9092}
mp.messaging.incoming.solicitud-creada.connector=smallrye-kafka
mp.messaging.incoming.solicitud-creada.topic=solicitud.creada
mp.messaging.outgoing.solicitud-aprobada.connector=smallrye-kafka
mp.messaging.outgoing.solicitud-aprobada.topic=solicitud.aprobada
mp.messaging.outgoing.solicitud-rechazada.connector=smallrye-kafka
mp.messaging.outgoing.solicitud-rechazada.topic=solicitud.rechazada
```

---

## Reglas de Seguridad y CORS

- **CORS deshabilitado** en este microservicio. Toda gestión de CORS es responsabilidad del `api-gateway`.
- No incluir dependencias de seguridad JWT en este módulo; la autenticación la valida el gateway.

---

## Dependencias Maven (sin versiones)

```xml
<dependency><groupId>io.quarkus</groupId><artifactId>quarkus-rest</artifactId></dependency>
<dependency><groupId>io.quarkus</groupId><artifactId>quarkus-rest-jackson</artifactId></dependency>
<dependency><groupId>io.quarkus</groupId><artifactId>quarkus-hibernate-orm-panache</artifactId></dependency>
<dependency><groupId>io.quarkus</groupId><artifactId>quarkus-jdbc-postgresql</artifactId></dependency>
<dependency><groupId>io.quarkus</groupId><artifactId>quarkus-messaging-kafka</artifactId></dependency>
<dependency><groupId>io.quarkus</groupId><artifactId>quarkus-hibernate-validator</artifactId></dependency>
<dependency><groupId>io.quarkus</groupId><artifactId>quarkus-smallrye-openapi</artifactId></dependency>
```
