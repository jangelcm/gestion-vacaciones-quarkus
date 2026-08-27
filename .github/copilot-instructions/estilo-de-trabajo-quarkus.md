# Estilo de Trabajo — Quarkus / Proyecto Gestión de Vacaciones

## Convenciones de Código

### Generales
- Java 17+. Usar `var` para inferencia local donde mejore la legibilidad.
- `record` para DTOs inmutables (requests, responses, eventos Kafka).
- No usar Lombok; Quarkus y los Java records cubren el boilerplate.
- Siempre añadir `@Path` con prefijo `/api/v1/<recurso>` en plural.
- Los métodos de los resources deben devolver `Response` cuando el código HTTP es relevante, o el tipo directamente (`List<Dto>`) para colecciones simples.

---

## Inyección de Dependencias

- Usar `@ApplicationScoped` para beans de larga vida (servicios, adaptadores, repositorios).
- Usar `@Inject` para DI por campo. En tests, preferir constructor injection.
- **Nunca** instanciar manualmente clases de negocio (`new ServiceImpl()`).
- Inyectar siempre la **interfaz de puerto** (`ports/in/` o `ports/out/`), no la implementación concreta.

```java
// ✅ Correcto
@Inject
AprobarSolicitudUseCase aprobarUseCase;

// ❌ Incorrecto
@Inject
AprobarSolicitudUseCaseImpl aprobarUseCase;
```

---

## DTOs

### Request DTOs (entrada HTTP)
- Usar `record` con anotaciones de validación Bean Validation.
- Colocar en `infrastructure/adapters/in/rest/dto/`.

```java
public record AprobarSolicitudRequest(
    @NotBlank(message = "El aprobadorId es requerido")
    String aprobadorId,
    String comentario
) {}
```

### Response DTOs (salida HTTP)
- Usar `record` con un método estático `fromDomain(DomainModel model)`.
- No exponer entidades JPA ni modelos de dominio directamente.

```java
public record AprobacionResponse(Long id, Long solicitudId, String estado, LocalDateTime fechaAprobacion) {
    public static AprobacionResponse fromDomain(Aprobacion a) {
        return new AprobacionResponse(a.getId(), a.getSolicitudId(), a.getEstado().name(), a.getFechaAprobacion());
    }
}
```

### Event DTOs (Kafka)
- Usar `record` inmutable.
- Colocar en `infrastructure/adapters/out/kafka/dto/` (productores) o `infrastructure/adapters/in/kafka/dto/` (consumidores).
- Serialización: `io.quarkus.kafka.client.serialization.ObjectMapperSerializer` para producir.
- Deserialización: `org.apache.kafka.common.serialization.StringDeserializer` + parsing manual con `ObjectMapper` para mayor flexibilidad.

---

## Manejo de Excepciones

### Excepciones de Dominio
- Lanzar `java.util.NoSuchElementException` cuando un recurso no se encuentra.
- Lanzar `java.lang.IllegalStateException` para transiciones de estado inválidas.

### Exception Mappers (JAX-RS)
- Crear un `@Provider` que implemente `ExceptionMapper<T>` en `infrastructure/config/`.
- Siempre retornar un JSON con estructura `{ "mensaje": "...", "timestamp": "..." }`.

```java
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<NoSuchElementException> {

    record ErrorResponse(String mensaje, String timestamp) {}

    @Override
    public Response toResponse(NoSuchElementException e) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(new ErrorResponse(e.getMessage(), LocalDateTime.now().toString()))
            .build();
    }
}
```

---

## Transacciones

- Anotar `@Transactional` en la **capa de aplicación** (use case implementations), no en los adaptadores de persistencia.
- Los adaptadores de repositorio (`infrastructure/adapters/out/persistence/`) ejecutan dentro de la transacción iniciada por el use case.
- Los métodos que solo leen datos pueden omitir `@Transactional` o usar `@Transactional(Transactional.TxType.SUPPORTS)`.

---

## Kafka con SmallRye Reactive Messaging

### Producción (Emitter)
```java
@Inject
@Channel("solicitud-aprobada")
Emitter<AprobacionEventoDTO> emitter;

public void publicar(AprobacionEventoDTO dto) {
    emitter.send(dto);
}
```

### Consumo (@Incoming)
```java
@Incoming("solicitud-creada")
public void consumir(String mensaje) {
    try {
        SolicitudCreadaEventDTO dto = objectMapper.readValue(mensaje, SolicitudCreadaEventDTO.class);
        // procesar
    } catch (JsonProcessingException e) {
        LOG.errorf("Error deserializando evento: %s", e.getMessage());
    }
}
```

### Configuración de canales (application.properties)
- Siempre mapear el nombre de canal SmallRye al topic Kafka real:
  - `mp.messaging.incoming.<canal>.topic=<topic.kafka>`
  - `mp.messaging.outgoing.<canal>.topic=<topic.kafka>`
- Usar `.` en nombres de topics Kafka (e.g., `solicitud.creada`) y `-` en canales SmallRye (e.g., `solicitud-creada`).

---

## Variables de Entorno y Configuración

- Externalizar credenciales y URLs usando la sintaxis de Quarkus:
  ```properties
  quarkus.datasource.password=${DB_PASS:defaultlocal}
  ```
- Para desarrollo local, el `defaultlocal` es el valor de fallback.
- En producción, inyectar variables de entorno reales (Docker/Kubernetes).

---

## Logging

- Usar `org.jboss.logging.Logger` (el logger estándar de Quarkus).
- Declarar el logger como campo `static final`:

```java
private static final Logger LOG = Logger.getLogger(MiClase.class);
```

- Usar métodos de formato (`LOG.infof`, `LOG.errorf`) para evitar concatenación de strings.

---

## OpenAPI / Swagger

- Incluir `quarkus-smallrye-openapi` en cada microservicio con endpoints REST.
- Swagger UI disponible en desarrollo en `/q/swagger-ui`.
- Documentar los endpoints con `@Operation`, `@APIResponse` de MicroProfile OpenAPI si son endpoints públicos críticos.

---

## Estructura Maven Multi-módulo

- **El `pom.xml` padre** gestiona versiones vía `<dependencyManagement>` (importa el BOM de Quarkus).
- **Los módulos hijos** declaran dependencias **sin versión**.
- Los módulos hijos **no deben redeclarar** `<dependencyManagement>` ni las propiedades de versión del padre.
- Cada microservicio referencia al padre con `<parent>`.

```xml
<!-- ✅ Correcto en módulo hijo -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-jackson</artifactId>
</dependency>

<!-- ❌ Incorrecto: no declarar versión en módulo hijo -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-jackson</artifactId>
    <version>3.35.2</version>
</dependency>
```
