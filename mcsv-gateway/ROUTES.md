# `routes.json` — cómo declarar rutas del gateway

Este archivo (`src/main/resources/routes.json`) es donde se declaran las rutas manuales del
gateway: qué path reenviar, a qué backend, y con qué reglas. Es el reemplazo de la vieja lista
`MANUAL_ROUTES` en `GatewayRoute.java` — ahora para agregar una ruta **no se toca código Java**,
solo este JSON.

## 1. ¿Dónde vive y quién lo lee?

```
src/main/resources/routes.json
```

Al arrancar, `GatewayRoute.init(...)` (en `GatewayRoute.java`) busca este archivo en el
classpath y lo carga con Jackson. El nombre del archivo se puede cambiar con la propiedad
`gateway.routes.file` en `application.properties` (por defecto es `routes.json`), pero lo normal
es no tocar eso.

> Como el archivo vive en `src/main/resources`, queda empaquetado dentro del jar/imagen nativa.
> Si lo editás, hace falta reiniciar (en modo dev con `./mvnw quarkus:dev` esto pasa solo, con
> hot-reload).

## 2. Forma general del archivo

Es un **array JSON**, donde cada elemento es una ruta. `targets` es a su vez un array —
normalmente con **un solo backend**, pero admite **más de uno** para repartir carga entre ellos
(ver sección 4):

```json
[
  { "path": "...", "targets": [{ "host": "...", "port": 0 }], "requiresJwt": false, "stripPrefix": false },
  { "path": "...", "targets": [{ "host": "...", "port": 0 }], "requiresJwt": false, "stripPrefix": false }
]
```

Ejemplo real (el que ya está en el proyecto):

```json
[
  { "path": "/mcsv-auth/auth/login", "targets": [{ "host": "localhost", "port": 9001 }], "requiresJwt": false, "stripPrefix": true },
  { "path": "/mcsv-auth/users",      "targets": [{ "host": "localhost", "port": 9001 }], "requiresJwt": true,  "stripPrefix": true },
  { "path": "/mcsv-auth/auth",       "targets": [{ "host": "localhost", "port": 9001 }], "requiresJwt": true,  "stripPrefix": true },
  { "path": "/orders",               "targets": [{ "host": "localhost", "port": 9002 }], "requiresJwt": true,  "stripPrefix": false }
]
```

## 3. Los campos, uno por uno

| Campo | Tipo | Obligatorio | Qué hace |
|---|---|---|---|
| `path` | string | sí | El prefijo de URL que activa esta ruta. **Debe empezar con `/`**. |
| `targets` | array de `{host, port}` | sí, al menos uno | Los backends destino. Con uno solo, todo el tráfico de esta ruta va ahí. Con varios, el gateway reparte las peticiones entre ellos (round-robin, ver sección 4). |
| `requiresJwt` | booleano | no (default `false`) | Si es `true`, exige un header `Authorization: Bearer <token>` con un JWT válido antes de reenviar. Si falta o es inválido, responde `401` y **no** llega al backend. |
| `stripPrefix` | booleano | no (default `false`) | Si es `true`, antes de reenviar le saca el primer segmento del path a la URL. Ver ejemplo más abajo. |

### `path`

Cualquier petición cuyo path **empiece** con este prefijo matchea la ruta. Internamente se
convierte en un patrón `path/*`, así que `/orders` matchea `/orders`, `/orders/123`,
`/orders/123/items`, etc.

### `requiresJwt`

```
requiresJwt: false  →  ruta pública, no pide token
requiresJwt: true   →  exige "Authorization: Bearer <jwt-válido>", si no, 401
```

### `stripPrefix`

Controla si se le "saca" el primer segmento a la URL antes de mandarla al backend. Sirve para
que el backend no tenga que saber con qué prefijo lo expuso el gateway.

```
stripPrefix: false                          stripPrefix: true
Cliente pide:                               Cliente pide:
  /orders/123                                 /mcsv-auth/auth/login
                     ↓                                          ↓
Backend recibe:                             Backend recibe:
  /orders/123   (igual, sin cambios)          /auth/login   (sin el primer segmento)
```

Regla mental: si el prefijo del `path` (`/mcsv-auth`, `/orders`, etc.) es solo un "namespace" que
el backend no conoce ni espera, poné `stripPrefix: true`. Si el backend sí espera ese prefijo tal
cual, dejalo en `false` (o no lo declares, porque `false` es el default).

## 4. Balanceo de carga: más de un `target` por ruta

Si un mismo microservicio corre en varias instancias (ej. `orders` levantado dos veces en un
`docker-compose`, cada una en un puerto o container distinto), declará todas en `targets`:

```json
{ "path": "/orders", "targets": [
    { "host": "orders-1", "port": 9002 },
    { "host": "orders-2", "port": 9002 },
    { "host": "orders-3", "port": 9002 }
  ], "requiresJwt": true, "stripPrefix": false }
```

El gateway reparte las peticiones entre esos targets con **round-robin**: la 1ª petición va a
`orders-1`, la 2ª a `orders-2`, la 3ª a `orders-3`, la 4ª otra vez a `orders-1`, y así
sucesivamente en orden circular. Cada target tiene su propio pool de conexiones (`HttpClient`)
independiente, así que si uno se cae o se pone lento, no afecta el pool de los demás — aunque
las peticiones que le tocaron a ese target sí van a fallar (el round-robin de este gateway **no
hace health-check**: no detecta automáticamente que un target está caído y lo sigue usando en su
turno).

> **Docker Compose:** usá nombres de servicio fijos (`orders-1`, `orders-2`, ...) para cada
> réplica en tu `docker-compose.yml`, no `docker compose up --scale`. El motivo es que el
> `HttpClient` del gateway reutiliza conexiones (`keep-alive`); con `--scale`, el DNS interno de
> Docker reparte IPs pero el cliente ya tiene la conexión abierta con una sola, así que en la
> práctica no balancea. Con servicios nombrados y `targets` explícitos, el balanceo sí lo hace el
> gateway correctamente, ruta por ruta.

## 5. El orden de las rutas en el array importa

Las rutas se evalúan **en el orden en que aparecen en el archivo**, de arriba hacia abajo, y gana
la **primera que matchee**. Esto importa cuando una ruta es más específica que otra:

```json
[
  { "path": "/mcsv-auth/auth/login", "targets": [{ "host": "localhost", "port": 9001 }], "requiresJwt": false, "stripPrefix": true },
  { "path": "/mcsv-auth/auth",       "targets": [{ "host": "localhost", "port": 9001 }], "requiresJwt": true,  "stripPrefix": true }
]
```

`/mcsv-auth/auth/login` es un caso particular *dentro* de `/mcsv-auth/auth/*`. Por eso va
**primero**: así el login queda público (`requiresJwt: false`), y el resto de `/mcsv-auth/auth/*`
sigue exigiendo JWT. Si el orden estuviera invertido, `/mcsv-auth/auth` (más genérica) matchearía
primero y `/mcsv-auth/auth/login` nunca se alcanzaría — el login quedaría protegido por error.

**Regla:** ruta específica arriba, ruta genérica abajo.

Cualquier petición que no matchee ninguna entrada de `routes.json` cae al backend por defecto
(`gateway.target.host` / `gateway.target.port` en `application.properties`).

## 6. Cómo agregar una ruta nueva — guía paso a paso

1. Abrí `src/main/resources/routes.json`.
2. Agregá un objeto nuevo al array (no olvides la coma del elemento anterior):
   ```json
   { "path": "/productos", "targets": [{ "host": "localhost", "port": 9003 }], "requiresJwt": true, "stripPrefix": false }
   ```
3. Si esta ruta es un caso particular de otra ya existente (ej. agregás
   `/productos/destacados` y ya existe `/productos`), ubicala **antes** que la genérica.
4. Guardá el archivo. En modo dev (`./mvnw quarkus:dev`) se recarga solo.
5. Probá:
   ```bash
   curl http://localhost:8080/productos
   ```

No hace falta recompilar manualmente, ni tocar `GatewayRoute.java`, ni reiniciar nada a mano en
modo dev.

## 7. Errores comunes

| Síntoma | Causa probable |
|---|---|
| La app no arranca / log de error al leer el archivo | JSON mal formado: falta una coma, una llave, o quedó una coma sobrante al final del array. |
| Log: `No se encontró 'routes.json' en el classpath` | El archivo no existe en `src/main/resources/`, o se cambió `gateway.routes.file` a otro nombre sin crear ese archivo. |
| Log: `Ruta inválida ignorada: ...` | El `path` no empieza con `/`. |
| Log: `Ruta sin 'targets' ignorada: ...` | El array `targets` está vacío o falta en esa entrada. |
| Una ruta específica nunca se activa (siempre cae en la genérica) | Está declarada **después** de una ruta más genérica en el array — moverla arriba. |
| El backend recibe el path con el prefijo que no esperaba | Falta `stripPrefix: true` en esa ruta. |
| El backend recibe el path sin el prefijo que sí esperaba | `stripPrefix` está en `true` y debería estar en `false` (o simplemente no declararlo). |
| `401` en una ruta que debería ser pública | `requiresJwt` quedó en `true` (o no se declaró y por descuido se esperaba que fuera pública — el default es `false`, revisar el JSON). |
| Con varios `targets`, siguen fallando peticiones aunque el resto de instancias esté sana | El round-robin no hace health-check — sigue mandando tráfico al target caído en su turno. Sacalo de `targets` mientras esté abajo. |

## 8. Alternativa rápida sin editar el JSON (para pruebas puntuales)

También existe `gateway.routes` en `application.properties`, para declarar rutas por variable de
entorno o config sin tocar ningún archivo del código fuente:

```properties
gateway.routes=/reportes:localhost:9004:jwt+strip
```

Formato: `/path:host:port` o `/path:host:port:flags`, con `flags` = `jwt`, `strip`, o `jwt+strip`.
Útil para overrides en un ambiente puntual (ej. variable de entorno en un contenedor), pero para
rutas permanentes del proyecto lo recomendado sigue siendo `routes.json`.
