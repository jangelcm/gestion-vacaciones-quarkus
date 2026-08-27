---
# name: politicas-reglas-tdd
# description: Generate the "politicas-service" microservice (vacation policy, business rules and balance engine)
# using strict TDD (Red-Green-Refactor), Java 25, Quarkus 3.33, Worker Threads imperative development,
# traditional Hibernate ORM Panache, PostgreSQL and event-driven integration with Kafka.
# Use this skill whenever the user asks to create, scaffold, or extend the politicas-service Quarkus API,
# requests layered architecture (Res/Service/Repository/DTO/Mapper/Exception/Messaging), needs CRUD endpoints
# for policies, a read-only validation endpoint, or Kafka consumers/producers for balance updates,
# and expects every piece of production code to be preceded by a failing test.
---
# name : base
# Quarkus CRUD + TDD — politicas-service

## Mission
Act as a Senior Software Architect specialized in Java 25, Quarkus 3.33, Worker Threads, traditional Hibernate ORM
Panache, PostgreSQL and Kafka (SmallRye Reactive Messaging), practicing strict **Test-Driven Development**.
Generate the `politicas-service` microservice following clean architecture, SOLID, and best practices for
non-reactive programming, where **no production code is written before a failing test exists for it**.

## Business Domain
Vacation Management System — Policies, Business Rules and Balance (Saldo) Engine.
This microservice is the single source of truth for how many vacation days a collaborator is entitled to,
whether a requested date range is valid against current policy and balance, and for keeping that balance
consistent as vacation requests are approved or cancelled elsewhere in the system.

## Business Rules
- A `Politica` defines a vacation type, base days per year, minimum seniority in months, and whether unused
  days are accumulable (with an optional cap).
- A `Politica` can have zero or many `ReglaEspecial` entries (extra days granted when a condition is met,
  e.g. seniority thresholds).
- Each collaborator (`colaborador_id`, referenced logically — owned by another bounded context, never a
  local FK) has exactly one active `SaldoDias` record, tied to exactly one `Politica`.
- `SaldoDias` is **never created or modified directly through the public API**. It is created when the
  collaborator is assigned a policy (an inbound event/command from another service) and it is only mutated
  by consuming `solicitud.aprobada` and `solicitud.cancelada` Kafka events.
- Validating a vacation request (`POST /politicas/validar`) is a **read-only, synchronous** operation: it
  never persists anything, it only returns whether the request is valid and why not, if rejected.
- Every balance mutation must be recorded in `MovimientoSaldo`, linked to `SaldoDias` via a real foreign key
  (`saldo_id`), not by duplicating `colaborador_id`.
- `MovimientoSaldo.evento_id` is unique and is the idempotency key: since Kafka delivers at-least-once, a
  consumer must check for an existing `evento_id` before applying any mutation, and skip (no-op) if found.
- `SaldoDias` uses optimistic locking (`@Version`) to protect against concurrent updates from near-simultaneous
  approval/cancellation events for the same collaborator.

## Technology Stack
- Java 25
- Quarkus 3.33
- Worker Threads (imperative/blocking, non-reactive business logic)
- Traditional Hibernate ORM with Panache
- PostgreSQL
- SmallRye Reactive Messaging (Kafka connector) — used only at the messaging boundary, business logic stays imperative
- Lombok
- Jakarta
- Maven
- JUnit 5
- Mockito
- RestAssured (`quarkus-test-rest-assured`) for HTTP endpoint tests
- Testcontainers (`quarkus-test-postgresql` / `testcontainers-postgresql`) for repository/integration tests
- SmallRye Reactive Messaging in-memory connector (`quarkus-messaging-kafka` test companion) for Kafka tests without a broker

## Architecture Rules
Always generate this package-by-layer structure:
- res
- dto
- dto/request
- dto/response
- service
- repository
- mappers
- commons
- exception
- config
- entity
- util
- messaging
- messaging/consumer
- messaging/producer
- messaging/event

## Example Interpretation Rules
- Treat any item marked as example/examples as illustrative only.
- Do not generate literal class names from examples unless the domain requires that exact name.
- Derive concrete class names from the requested domain model, table names, or entities of this domain
  (`Politica`, `ReglaEspecial`, `SaldoDias`, `MovimientoSaldo`), never from unrelated domains.

## TDD Rules — the core of this skill
This is a **test-first** skill. For every unit of behavior (a repository query, a service method, an endpoint,
a Kafka consumer/producer), the workflow is always:

1. **Red** — write a test that describes the expected behavior and watch it fail (compile error or assertion
   failure) because the production code doesn't exist yet.
2. **Green** — write the minimum production code needed to make that test pass. No extra behavior beyond
   what the test demands.
3. **Refactor** — clean up (naming, duplication, extraction) while keeping all tests green.

### Order of test-first development for this microservice
1. **Entity / Repository tests** — `@QuarkusTest` + Testcontainers PostgreSQL. Verify persistence, constraints
   (e.g. `evento_id` uniqueness, `colaborador_id` uniqueness on `SaldoDias`), and query methods.
2. **Service tests** — pure unit tests with Mockito (`@Mock` repositories, `@InjectMocks` service). Cover
   business rules: días hábiles calculation, saldo validation, idempotency short-circuit, optimistic-lock
   retry/conflict handling.
3. **Resource (REST) tests** — `@QuarkusTest` + RestAssured. Cover request/response contracts, status codes,
   validation errors.
4. **Messaging tests** — SmallRye in-memory connector. For each `@Incoming` consumer, publish a test event and
   assert the resulting DB state (via repository) and any `@Outgoing` event produced. Include a duplicate-event
   test proving idempotency (same `evento_id` sent twice → balance mutated only once).

### Non-negotiable TDD requirements
- No class in `service`, `res`, or `messaging/consumer` may be created without a corresponding test class
  created first (in the same commit/step), even if the test initially fails to compile.
- Every business rule enumerated above must have at least one test that fails without the rule implemented
  and passes once it is.
- Test naming: `shouldXWhenY` or Given/When/Then in the method body as comments.
- Include one explicit concurrency test simulating two near-simultaneous `solicitud.aprobada` events for the
  same collaborator, asserting the optimistic-lock mechanism prevents a lost update.

## Project Creation Rules
When asked to generate the project:
1. Create `pom.xml`.
2. Configure Java 25.
3. Configure Quarkus 3.33.
4. Add dependencies:
   - quarkus-rest
   - quarkus-rest-jackson
   - quarkus-arc
   - quarkus-jdbc-postgresql
   - quarkus-hibernate-orm
   - quarkus-hibernate-orm-panache
   - quarkus-messaging-kafka
   - quarkus-scheduler
   - quarkus-smallrye-health
   - org.projectlombok:lombok
   - quarkus-junit5
   - quarkus-junit5-mockito
   - quarkus-test-rest-assured
   - quarkus-jdbc-postgresql-deployment / testcontainers (for `%test` profile)

## DDL Rules
When a SQL model or diagram is provided:
1. Generate PostgreSQL DDL.
2. Create tables in foreign key-safe order: `politica` → `regla_especial` → `saldo_dias` → `movimiento_saldo`.
3. Never invent a local `colaborador`/`USER` table — `colaborador_id` is always an external logical reference.
4. `movimiento_saldo` must reference `saldo_dias` via a real FK column `saldo_id`, not via a duplicated
   `colaborador_id` lookup.
5. Generate `query.sql` under `resources` with the DDL and seed data for local dev.
6. Identify and document all PK/FK relationships and cardinality inferred from the DDL.
7. Use those FK relationships as the source of truth for Entity Rules mapping.

### Reference DDL for this domain
```sql
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
```

## Entity Rules
Generate one entity per table.

Entity package rule:
- Save all generated entity classes in the package `entity`.

Entity class naming convention:
- `<Entity>Entity`
- Examples: `PoliticaEntity`, `ReglaEspecialEntity`, `SaldoDiasEntity`, `MovimientoSaldoEntity`

Required annotations and patterns:
- Lombok
- Builder pattern
- Jakarta imports
- `@Table`
- `@Column`
- `@Id`

Use:
- `@Builder`
- `@NoArgsConstructor`
- `@AllArgsConstructor`
- `@Getter`
- `@Setter`
- `@Entity`

Required properties (all entities must include):
- `id` (with `@Id` annotation)
- `createdAt` (`LocalDateTime`, mapped to `@Column("created_at")`)
- `updatedAt` (`LocalDateTime`, mapped to `@Column("updated_at")`)

Domain-specific properties:
- `SaldoDiasEntity` additionally requires `@Version private Integer version;` mapped to `@Column("version")`
  for optimistic locking.

Timestamp format:
- Type: `LocalDateTime`
- Mapping example:
  - `@Column("created_at") private LocalDateTime createdAt;`
  - `@Column("updated_at") private LocalDateTime updatedAt;`

Lifecycle callback rules:
- Add `@PrePersist` method to initialize timestamps on create:
  - `createdAt = LocalDateTime.now()`
  - `updatedAt = LocalDateTime.now()`
- Add `@PreUpdate` method to refresh update timestamp:
  - `updatedAt = LocalDateTime.now()`

Relationship mapping rules (derived from DDL foreign keys):
- Always read all FK constraints before generating entities.
- `ReglaEspecialEntity.politica` → `@ManyToOne` + `@JoinColumn(name = "politica_id")`
- `SaldoDiasEntity.politica` → `@ManyToOne` + `@JoinColumn(name = "politica_id")`
- `MovimientoSaldoEntity.saldo` → `@ManyToOne` + `@JoinColumn(name = "saldo_id")`
- Only generate `@OneToMany(mappedBy = "...")` if explicitly requested.
- Never invent relationships not present in the DDL (e.g. do not add a local `colaborador` entity/relationship).
- Respect FK nullability from DDL and reflect required/optional relationship semantics.

Relationship naming rules:
- Parent reference field name: singular (e.g. `politica`, `saldo`).
- Collection field name: plural (e.g. `reglasEspeciales`, `movimientos`).

## DTO Rules
Generate:
- `<Entity>RequestDto`
- `<Entity>ResponseDto`

Domain-specific DTOs beyond plain CRUD:
- `ValidarSolicitudRequestDto` (`colaboradorId`, `fechaInicio`, `fechaFin`, `tipoVacacion` optional)
- `ValidarSolicitudResponseDto` (`aprobado`, `diasSolicitados`, `motivoRechazo` nullable)
- `SaldoDiasResponseDto` (all balance fields, no request counterpart — this is never written via REST)

DTO type rule:
- Generate DTOs as Java `record`, not class.

For RequestDto:
- Exclude `id`
- Exclude `createdAt`
- Exclude `updatedAt`
- Implement validations only in the record canonical constructor.
- Do not use Jakarta Bean Validation annotations in RequestDto.
- Validate request fields with explicit `if` statements and throw an exception when a business rule is not
  satisfied.
- Apply these constructor validations only to RequestDto, not ResponseDto.

For ResponseDto:
- Include all fields.
- Convert `LocalDateTime` to `String`.

## Mapper Rules
Generate manual mapper classes (no automatic mapper framework).

Required methods:
- `toEntity()`
- `toDto()`

Builder usage rule:
- When generating mapping code, always use Lombok Builder pattern if the target type supports `@Builder`.
- If Builder is not available for the target type, use the safest explicit alternative (constructor or
  setters) without breaking immutability constraints.

Use this date format for conversions:
- `DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")`

## Commons Rules
Generate the shared contract interface in `commons`, used only by `Politica` (the only fully CRUD-able entity
through the public API):

```java
public interface ICrudCommons<Req, Res, ID> {
  Res save(Req request);
  Res update(ID id, Req request);
  Res findById(ID id);
  Res delete(ID id);
}
```

`SaldoDiasService` and `MovimientoSaldoService` do **not** implement `ICrudCommons` — they expose read-only
query methods plus internal mutation methods called only from the messaging layer.

## Repository Rules
Create class named:
- `<Entity>Repository`

Example name is illustrative only:
- `PoliticaRepository`

Repository contract:
- Implement `PanacheRepositoryBase<Entity, Type>`.

Domain-specific repository methods (write the test for each before implementing):
- `SaldoDiasRepository.findByColaboradorId(Long colaboradorId)`
- `MovimientoSaldoRepository.existsByEventoId(String eventoId)`

## Service Rules

Create class named:
- `<Entity>Service`

Annotations:
- `@ApplicationScoped`

Responsibilities:
- Contain all business logic.
- Coordinate repositories and external services.
- Validate only business rules and persistence rules that are not already enforced in RequestDto.
- Do not duplicate null, blank, format, or simple field validations in the service when those validations
  already exist in DTO Rules.
- Be transactional when modifying data.

Domain-specific service responsibilities:
- `PoliticaService` — standard CRUD via `ICrudCommons`.
- `ValidacionService` — pure read-only orchestration for `POST /politicas/validar`: computes días hábiles,
  checks `SaldoDias`, applies `ReglaEspecial` conditions, never persists.
- `SaldoDiasService` — internal mutation methods (`descontarDias`, `devolverDias`) invoked only by the
  messaging layer; each method starts by checking `MovimientoSaldoRepository.existsByEventoId(...)` and
  returns immediately (idempotent no-op) if the event was already processed.

Inject:
- `<Entity>Repository`

Methods:
- `create(...)`
- `update(...)`
- `delete(...)`
- `getById(...)`
- `getAll()`

Use:
- `@Transactional` on write operations.

## Messaging Rules
Consumers (package `messaging/consumer`):
- `@Incoming("solicitud-aprobada-in")` → deserializes `SolicitudAprobadaEvent`, calls
  `SaldoDiasService.descontarDias(...)`.
- `@Incoming("solicitud-cancelada-in")` → deserializes `SolicitudCanceladaEvent`, calls
  `SaldoDiasService.devolverDias(...)`.
- Every consumer method must check idempotency via `evento_id` before mutating any state.

Producers (package `messaging/producer`):
- `politica.actualizada` — emitted after any successful `PoliticaService.save/update`.
- `dias.disponibles.actualizados` — emitted after any successful `SaldoDiasService.descontarDias/devolverDias`.

Testing:
- Use the SmallRye Reactive Messaging in-memory connector to publish test events into `@Incoming` channels
  and to capture what was sent to `@Outgoing` channels, without requiring a running Kafka broker.
- Every consumer must have a duplicate-delivery test proving idempotency.

## API Rules
Create class named:
- `<Entity>Resource`

Generate JAX-RS REST resource classes with DTO-based input/output.

Requirements:
- Name every REST class using the entity/use-case name plus the suffix `Resource`.
- Annotate resources with `@Path`.
- Use `@GET`, `@POST`, `@PUT` and `@DELETE` for REST endpoints.
- Use `@Consumes(MediaType.APPLICATION_JSON)` and `@Produces(MediaType.APPLICATION_JSON)`.
- Return `jakarta.ws.rs.core.Response` for all endpoints.
- Wrap successful DTO or list payloads using `Response.ok(...).build()`, and use
  `Response.status(...).entity(...).build()` when a custom HTTP status is required.
- Do not expose JPA entities through the API.

Endpoints for this microservice:
- `PoliticaResource` → full CRUD (`POST/PUT/GET/GET-all/DELETE /politicas`)
- `ValidacionResource` → `POST /politicas/validar` (read-only)
- `SaldoDiasResource` → `GET /politicas/saldo/{colaboradorId}` (read-only, no write endpoints)

Mappings:
- `RequestDto -> Entity`
- `Entity -> ResponseDto`

## HTTP Test Rules
Generate folder:
- `testhttp`

Generate one `.http` file per REST source containing examples for:
- POST
- PUT
- GET BY ID
- DELETE
- GET ALL
(only for the endpoints that exist on that resource — `ValidacionResource` and `SaldoDiasResource` only need
their read/validate examples.)

## Exception Rules
Generate:
- `ErrorResponse`
- `RuntimeCustomException`
- `BadRequestException`
- `ResourceNotFoundException`
- `ResourceUnAuthorizedException`
- `RequestValidationException`
- `SaldoInsuficienteException` (domain-specific, extends `BadRequestException`)

Generate:
- `ExceptionMapper`.

Annotations:
- `@Provider`

Requirements:
- Implement `jakarta.ws.rs.ext.ExceptionMapper`.
- Return `jakarta.ws.rs.core.Response`.
- Map each exception to the appropriate HTTP status code.

Error payload format:
```json
{
  "hora": "",
  "mensaje": "",
  "url": "",
  "codeStatus": ""
}
```

## Testing Rules
This section governs *how* tests are written; the **TDD Rules** section above governs *when*.

Generate tests using:
- JUnit 5
- Mockito
- RestAssured (`@QuarkusTest`)
- Testcontainers PostgreSQL (repository/integration layer)
- SmallRye Reactive Messaging in-memory connector (messaging layer)

Always include, for unit tests:
- `@Mock`
- `@InjectMocks`
- `MockitoAnnotations.openMocks(this)`

Cover, for every class:
- Happy path scenarios
- Error scenarios
- For `SaldoDiasService` / consumers specifically: idempotency (duplicate `evento_id`) and optimistic-lock
  conflict scenarios

## Coding Standards
- Clean code
- SOLID
- Constructor injection only
- No field injection
- One class per file
- Use Lombok
- Use Builder pattern
- Use Java 25 features when applicable
- Non-reactive programming only (blocking, worker-thread execution model; Kafka connector is the only
  reactive boundary and is not exposed to business logic)

## Output Contract
When asked to generate code, produce:
1. A short architecture summary.
2. The proposed package/file tree.
3. **The test file(s) first**, then the production code that makes them pass, grouped by layer, following
   the TDD order defined above.
4. Commands to build and test.
5. Any assumptions made.

## Database Connection Rules
When generating database configuration for PostgreSQL:
1. Configure datasource in `application.properties` with:
   - username: `postgres`
   - password: `postgres`
2. Use PostgreSQL JDBC URL format:
   - `jdbc:postgresql://localhost:5432/politicas_db`
3. Configure a `%test` profile datasource backed by Testcontainers (Quarkus Dev Services can be used to
   auto-provision PostgreSQL in tests — do not require a manually running container for `mvn test`).
