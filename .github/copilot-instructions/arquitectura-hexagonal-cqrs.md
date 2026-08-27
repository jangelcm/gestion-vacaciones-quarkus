# Arquitectura Hexagonal + CQRS — Normas del Proyecto

## Principios fundamentales

Este proyecto sigue **Arquitectura Hexagonal (Ports & Adapters)** con separación de responsabilidades en tres capas:
`domain` → `application` → `infrastructure`

La regla de dependencia es **unidireccional hacia adentro**:  
`infrastructure` puede conocer a `application` y `domain`, pero `domain` **nunca** debe importar clases de `application` ni de `infrastructure`.

---

## Estructura de paquetes por módulo

```
com.jacm.<servicio>/
├── domain/
│   ├── model/          # Entidades de dominio puras (sin anotaciones JPA/Jackson)
│   └── ports/
│       ├── in/         # Interfaces de casos de uso (puertos de entrada)
│       └── out/        # Interfaces de repositorios y publicadores (puertos de salida)
│
├── application/
│   └── usecases/       # Implementaciones de los casos de uso (@ApplicationScoped)
│
└── infrastructure/
    └── adapters/
        ├── in/
        │   ├── rest/   # JAX-RS Resources + DTOs HTTP
        │   └── kafka/  # Consumidores SmallRye Reactive Messaging
        └── out/
            ├── persistence/  # Entidades JPA + Repositorios Panache
            └── kafka/        # Productores / Emitters SmallRye
```

---

## Capa Domain

### Reglas
- Las clases en `domain/model/` son **POJOs puros**: sin `@Entity`, sin anotaciones Jackson, sin dependencias externas.
- Pueden contener **lógica de negocio** (métodos que cambian estado, validaciones de dominio).
- Los **puertos de entrada** (`ports/in/`) son interfaces Java que definen los casos de uso. Nombrarlos como `<Acción><Entidad>UseCase`.
- Los **puertos de salida** (`ports/out/`) son interfaces que abstraen persistencia y mensajería. Nombrarlos como `<Entidad>RepositoryPort` y `<Entidad>EventPublisherPort`.

### Ejemplo de entidad de dominio
```java
// ✅ Correcto: POJO puro con lógica de negocio
public class Aprobacion {
    private Long id;
    private EstadoAprobacion estado;

    public void aprobar(String aprobadorId, String comentario) {
        this.estado = EstadoAprobacion.APROBADO;
        this.aprobadorId = aprobadorId;
        this.fechaAprobacion = LocalDateTime.now();
    }
}

// ❌ Incorrecto: no colocar @Entity en el modelo de dominio
```

---

## Capa Application

### Reglas
- Solo contiene **implementaciones de casos de uso** (`ports/in/`).
- Anotadas con `@ApplicationScoped`.
- Inyectan **únicamente interfaces de puertos** (`ports/out/`), nunca clases concretas de infraestructura.
- Las operaciones que modifican estado deben anotarse con `@Transactional`.
- No contienen lógica HTTP, Kafka ni JPA.

```java
// ✅ Correcto: inyectar puerto, no implementación concreta
@ApplicationScoped
public class AprobarSolicitudUseCaseImpl implements AprobarSolicitudUseCase {

    @Inject
    AprobacionRepositoryPort repositoryPort;   // puerto de salida (interfaz)

    @Inject
    AprobacionEventPublisherPort eventPort;    // puerto de salida (interfaz)
}

// ❌ Incorrecto: inyectar clase de infraestructura directamente
// @Inject AprobacionPanacheRepository panacheRepo;
```

---

## Capa Infrastructure

### Reglas generales
- Los adaptadores **implementan** las interfaces de puertos (`implements XxxRepositoryPort`).
- Las entidades JPA (`@Entity`) residen en `adapters/out/persistence/`, nunca en `domain/`.
- Las entidades JPA tienen métodos `fromDomain(DomainModel)` y `toDomain()` para el mapeo.
- Los adaptadores REST (`@Path`) residen en `adapters/in/rest/`.
- Los consumidores Kafka residen en `adapters/in/kafka/`.
- Los productores/emitters Kafka residen en `adapters/out/kafka/`.

### Naming conventions

| Tipo                    | Sufijo / Ubicación                             | Ejemplo                          |
|-------------------------|------------------------------------------------|----------------------------------|
| Entidad JPA             | `JpaEntity`                                    | `AprobacionJpaEntity`            |
| Repositorio Panache     | `PanacheRepository`                            | `AprobacionPanacheRepository`    |
| Adapter de repositorio  | `RepositoryAdapter` (impl. port)               | `AprobacionRepositoryAdapter`    |
| Adapter de eventos      | `EventPublisherAdapter` (impl. port)           | `AprobacionEventPublisherAdapter`|
| Consumer Kafka          | `Consumer` con `@Incoming("channel")`          | `SolicitudCreatedConsumer`       |
| Resource REST           | `Resource` con `@Path`                         | `AprobacionResource`             |
| DTO de Request          | `Request`                                      | `AprobarSolicitudRequest`        |
| DTO de Response         | `Response`                                     | `AprobacionResponse`             |
| DTO de Evento Kafka     | `EventoDTO`                                    | `AprobacionEventoDTO`            |

---

## Patrón CQRS en este proyecto

| Lado            | Microservicio            | Responsabilidad                             |
|-----------------|--------------------------|---------------------------------------------|
| **Comandos (Write)** | `ms-solicitudes`    | Crear / editar / cancelar solicitudes, publicar eventos |
| **Comandos (Write)** | `ms-aprobaciones`   | Aprobar / rechazar, consumir solicitud.creada, publicar resultado |
| **Consultas (Read)** | `ms-consultas`      | Leer desde MongoDB (proyecciones)           |

### Flujo de eventos Kafka

```
ms-solicitudes  ──► solicitud.creada ──►  ms-aprobaciones
                                                │
                         ┌──────────────────────┤
                         ▼                      ▼
              solicitud.aprobada      solicitud.rechazada
                         │                      │
                         └──────────┬───────────┘
                                    ▼
                             ms-solicitudes (actualiza estado local)
                             ms-consultas  (actualiza proyección)
```

---

## Reglas de CORS y Seguridad

- **CORS**: Centralizado exclusivamente en el `api-gateway`. **Ningún microservicio** debe habilitar `quarkus.http.cors.enabled=true`.
- **JWT**: La validación de tokens es responsabilidad del `api-gateway` y del `ms-autenticacion`.
- Las dependencias de versiones se declaran **únicamente en el `pom.xml` padre** vía `<dependencyManagement>`. Los módulos hijos no deben declarar versiones.
