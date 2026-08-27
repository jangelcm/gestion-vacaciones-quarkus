# mcsv-gateway

Gateway HTTP construido con **Quarkus** + **Vert.x**. Recibe todas las peticiones en un solo
puerto y las reenvía (reverse proxy) al microservicio correspondiente según el path de la URL.

## 1. Idea general

Un gateway es la puerta de entrada única a un conjunto de microservicios. En vez de que el
cliente llame directamente a `auth:9001`, `orders:9002`, etc., llama siempre a
`gateway:8080/lo-que-sea`, y el gateway decide a qué microservicio reenviar la petición según
el path.

```
Cliente → http://localhost:8080/auth/login   → mcsv-auth   (localhost:9001)
Cliente → http://localhost:8080/orders       → mcsv-orders (localhost:9002)
Cliente → http://localhost:8080/lo-que-sea   → backend por defecto (localhost:8081)
```

Todo el comportamiento vive en una sola clase: `GatewayRoute.java`.

## 2. Piezas técnicas que usa

| Pieza | Para qué sirve |
|---|---|
| `quarkus-vertx-http` | Le da a Quarkus un `Router` de Vert.x-Web donde registrar rutas HTTP |
| `vertx-http-proxy` | Hace el trabajo de reverse proxy real: toma una petición entrante y la reenvía a un host:puerto destino |
| CDI (`@ApplicationScoped`, `@Observes`) | Quarkus arranca `GatewayRoute` como un bean y le entrega el `Router` y el `Vertx` ni bien están listos |
| MicroProfile Config (`@ConfigProperty`) | Lee la configuración desde `application.properties` |

No hay un servidor HTTP propio: el gateway usa el mismo servidor HTTP que ya trae Quarkus,
solo le agrega rutas.

## 3. Configuración (`application.properties`)

```properties
quarkus.http.port=8080

gateway.target.host=localhost
gateway.target.port=8081
```

- `quarkus.http.port`: puerto donde escucha el gateway (la puerta de entrada única).
- `gateway.target.host` / `gateway.target.port`: backend **por defecto**. Cualquier petición
  que no matchee ninguna ruta específica termina acá. Es obligatorio: si falta, la app no
  arranca (ver paso 4).
- `gateway.path` (opcional, default `/`): sub-path desde el cual aplica el proxy por defecto.
- `gateway.maxPoolSize` (opcional, default `20`): tamaño del pool de conexiones HTTP que el
  gateway mantiene abierto hacia cada backend.
- `gateway.routes` (opcional): forma de declarar rutas específicas por configuración en vez de
  código, formato `/path:host:port,/otro:host:port`.

## 4. El constructor: validar configuración al arrancar

```java
public GatewayRoute(
        @ConfigProperty(name = "gateway.target.host") String targetHost,
        @ConfigProperty(name = "gateway.target.port") int targetPort,
        @ConfigProperty(name = "gateway.path", defaultValue = "/") String gatewayPath,
        @ConfigProperty(name = "gateway.maxPoolSize", defaultValue = "20") int maxPoolSize) {
    if (targetHost == null || targetHost.isBlank()) {
        throw new IllegalStateException("'gateway.target.host' es obligatorio");
    }
    ...
}
```

Quarkus inyecta la configuración directamente como parámetros del constructor. Si falta
`gateway.target.host` o el puerto es inválido, la aplicación **falla al arrancar** en vez de
fallar más tarde con cada petición — es preferible detectar un error de configuración de
inmediato.

## 5. `init(...)`: el punto de partida real

```java
void init(@Observes Router router, Vertx vertx) {
    this.router = router;
    this.vertx = vertx;
    ...
}
```

`@Observes Router router` es un evento CDI que Quarkus dispara automáticamente cuando el
`Router` HTTP ya está listo para recibir rutas. Ahí es donde el gateway:

1. Registra las rutas que vengan de `gateway.routes` (configuración).
2. Registra las rutas declaradas en código (`MANUAL_ROUTES`).
3. Registra el proxy por defecto (catch-all).

El orden de estos tres pasos en el código **no determina** qué ruta gana — eso lo controla
`order(...)`, explicado en el paso 7.

## 6. Cómo se declara una ruta

Una ruta es simplemente: *"las peticiones que empiecen con este path, reenvíalas a este
host:puerto"*. Se modela con un record:

```java
public record RouteDefinition(String path, String host, int port) { ... }
```

Hay dos formas de declararlas:

**A) En código** (lo que pediste agregar manualmente), editando la lista al principio de la
clase:

```java
private static final List<RouteDefinition> MANUAL_ROUTES = List.of(
        new RouteDefinition("/auth/login", "localhost", 9001),
        new RouteDefinition("/orders", "localhost", 9002)
);
```

**B) Por configuración**, en `application.properties`:

```properties
gateway.routes=/auth:localhost:9001,/orders:localhost:9002
```

`init(...)` parsea ese string (`RouteDefinition.parse`) y lo trata exactamente igual que una
ruta de `MANUAL_ROUTES` — ambos caminos terminan llamando a `addRoute(...)`.

## 7. `addRoute(...)`: registrar una ruta en el Router

```java
public synchronized void addRoute(RouteDefinition def) {
    ...
    HttpClient client = newClient();
    HttpProxy proxy = HttpProxy.reverseProxy(client).origin(def.port(), def.host());
    String pattern = def.path().endsWith("/*") ? def.path() : def.path() + "/*";
    Route route = router.route(pattern)
            .order(nextOrder++)
            .handler(rc -> proxy.handle(rc.request()));

    proxies.put(def.path(), new Proxied(route, client));
}
```

Paso a paso:

1. **`newClient()`**: crea un `HttpClient` de Vert.x dedicado a esta ruta (pool de conexiones
   propio hacia ese backend). Cada ruta tiene el suyo — así, si una ruta se borra y se cierra
   su cliente, no afecta a las demás.
2. **`HttpProxy.reverseProxy(client).origin(port, host)`**: crea el objeto que sabe reenviar
   una petición HTTP hacia `host:port` usando ese cliente.
3. **`path + "/*"`**: convierte `/auth/login` en el patrón `/auth/login/*`, que en Vert.x-Web
   matchea tanto el path exacto como cualquier sub-path (`/auth/login/refresh`, etc.).
4. **`.order(nextOrder++)`**: le da a esta ruta un número de prioridad único y creciente. Es
   clave para el punto 8.
5. **`.handler(rc -> proxy.handle(rc.request()))`**: cuando una petición matchea el patrón,
   Vert.x-Web ejecuta este handler, que delega la petición al proxy — el proxy hace el reenvío
   real (copia headers, body, stream de la respuesta, etc.).
6. Se guarda todo (`Route` + `HttpClient`) en el mapa `proxies`, indexado por path, para poder
   removerlo después.

## 8. El proxy por defecto y el orden de las rutas

```java
private void registerDefaultProxy() {
    ...
    Route route = (root ? router.route() : router.route(pattern))
            .order(DEFAULT_ROUTE_ORDER)  // 10_000
            .handler(rc -> proxy.handle(rc.request()));
    ...
}
```

El proxy por defecto matchea **todo** (`/*`, es decir, cualquier path). Si se registrara con
prioridad normal, capturaría cualquier petición antes de que las rutas específicas tuvieran
oportunidad de evaluarse — porque en Vert.x-Web, sin indicar orden explícito, las rutas se
evalúan en el orden en que se registraron.

Por eso:

- Las rutas específicas (`MANUAL_ROUTES`, `gateway.routes`, o agregadas en caliente con
  `addRoute`) reciben un `order` bajo (`0, 1, 2, ...` vía `nextOrder++`).
- El proxy por defecto siempre recibe `order = 10_000`.

Vert.x-Web evalúa las rutas de menor a mayor `order`, así que las rutas específicas **siempre**
se evalúan primero, sin importar cuándo se registraron. Si ninguna matchea, cae al proxy por
defecto.

## 9. Quitar una ruta

```java
public synchronized boolean removeRoute(String path) {
    Proxied p = proxies.remove(path);
    if (p == null) return false;
    p.route().remove();   // la saca del Router: deja de matchear peticiones
    p.client().close();   // cierra el pool de conexiones hacia ese backend
    return true;
}
```

Es simétrico a `addRoute`: limpia tanto la ruta del Router como el `HttpClient` asociado, para
no dejar conexiones abiertas colgando.

## 10. Apagado ordenado (`@PreDestroy`)

```java
@PreDestroy
void shutdown() {
    proxies.keySet().forEach(this::removeRoute);
}
```

Cuando Quarkus se apaga, se remueven todas las rutas y se cierran todos los `HttpClient`
(incluido el del proxy por defecto, guardado bajo la clave `__default__`). Evita conexiones
zombies o fugas de recursos al reiniciar en modo dev.

## 11. Cómo agregar una ruta nueva (guía rápida)

1. Abrí `src/main/java/com/example/gateway/GatewayRoute.java`.
2. Agregá una línea a `MANUAL_ROUTES`:
   ```java
   new RouteDefinition("/productos", "localhost", 9003)
   ```
3. Guardá. En modo dev (`./mvnw quarkus:dev`) Quarkus recarga la clase solo.
4. Probá: `curl http://localhost:8080/productos`.

No hace falta tocar nada más — ni reiniciar el gateway a mano, ni pegarle a ningún endpoint
administrativo (ese endpoint existió en una versión anterior y se eliminó por seguridad: no
tenía autenticación y permitía a cualquiera registrar un proxy hacia cualquier host).

## 12. Tests

- `GatewayRouteTest`: levanta el gateway completo con `@QuarkusTest` y verifica que una
  petición a `/hello` llegue al backend de prueba y devuelva su respuesta.
- `TestBackendResource`: backend HTTP mínimo (JDK `HttpServer`, sin Quarkus) que se levanta
  solo durante el test en el puerto `8082`, simulando el microservicio real.

```bash
./mvnw test
```

## 13. Correr en desarrollo

```bash
./mvnw quarkus:dev
```

Levanta el gateway en `http://localhost:8080` con hot-reload: cualquier cambio en
`GatewayRoute.java` (incluyendo `MANUAL_ROUTES`) se aplica sin reiniciar el proceso a mano.

> Nota: si un microservicio de backend queda colgado (deadlock, event-loop bloqueado), el
> gateway no puede "adivinarlo" — reenvía la petición y queda esperando una respuesta que
> nunca llega. Si una ruta específica se cuelga, primero probá pegarle directo al backend
> (`curl http://localhost:<puerto>/...`) para descartar que el problema esté en el gateway.
