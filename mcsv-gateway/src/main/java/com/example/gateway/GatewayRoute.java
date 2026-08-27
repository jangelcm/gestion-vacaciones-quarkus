package com.example.gateway;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.Future;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.httpproxy.HttpProxy;
import io.vertx.httpproxy.ProxyContext;
import io.vertx.httpproxy.ProxyInterceptor;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reverse proxy sobre el Router de Quarkus. Reenvía peticiones a otros servicios según
 * el path de la petición.
 *
 * Cómo agregar rutas:
 * - En código: agrega entradas a MANUAL_ROUTES.
 * - Por config: propiedad 'gateway.routes=/path:host:port,...' en application.properties.
 *
 * El proxy por defecto (gateway.target.host/port) siempre se evalúa al final, así nunca
 * tapa a las rutas específicas sin importar el orden en que se registraron.
 */
@ApplicationScoped
public class GatewayRoute {

    private static final Logger LOG = Logger.getLogger(GatewayRoute.class);
    private static final int DEFAULT_ROUTE_ORDER = 10_000;
    private static final String DEFAULT_KEY = "__default__";

    /**
     * Las rutas manuales ahora se declaran en src/main/resources/routes.json (ver
     * gateway.routes.file). Cada entrada admite:
     * - requiresJwt: exige un JWT válido en el header Authorization.
     * - stripPrefix: al reenviar, saca el primer segmento de la URL (el "namespace" del servicio).
     *   Ej. /mcsv-auth/auth/login llega al backend como /auth/login, sin importar qué tan
     *   específica sea la ruta que matcheó.
     *
     * IMPORTANTE: cuando dos rutas se superponen (una más específica que la otra, como
     * /mcsv-auth/auth/login vs /mcsv-auth/auth/*), la más específica debe ir ANTES en el
     * archivo JSON. El orden de las entradas define la prioridad (ver Route#order en
     * addRoute) — Vert.x no elige automáticamente "la más específica gana".
     *
     * private static final List<RouteDefinition> MANUAL_ROUTES = List.of(
     *         new RouteDefinition("/mcsv-auth/auth/login", "localhost", 9001, false, true), // público
     *         new RouteDefinition("/mcsv-auth/users", "localhost", 9001, true, true),        // resto de /auth, con JWT
     *         new RouteDefinition("/mcsv-auth/auth", "localhost", 9001, true, true),        // resto de /auth, con JWT
     *         new RouteDefinition("/orders", "localhost", 9002, true)
     * );
     */

    public record RouteDefinition(String path, List<Target> targets, boolean requiresJwt, boolean stripPrefix) {

        /** Un backend concreto al que se le puede reenviar tráfico para una ruta. */
        public record Target(String host, int port) {
        }

        public RouteDefinition(String path, String host, int port) {
            this(path, List.of(new Target(host, port)), false, false);
        }

        public RouteDefinition(String path, String host, int port, boolean requiresJwt) {
            this(path, List.of(new Target(host, port)), requiresJwt, false);
        }

        public RouteDefinition(String path, String host, int port, boolean requiresJwt, boolean stripPrefix) {
            this(path, List.of(new Target(host, port)), requiresJwt, stripPrefix);
        }

        static RouteDefinition parse(String entry) {
            // Formato: /path:host:port  o  /path:host:port:flags  (flags separados por '+': jwt, strip)
            String[] parts = entry.split(":");
            if (parts.length != 3 && parts.length != 4) {
                return null;
            }
            try {
                boolean jwt = false;
                boolean strip = false;
                if (parts.length == 4) {
                    for (String flag : parts[3].trim().split("\\+")) {
                        jwt |= "jwt".equalsIgnoreCase(flag);
                        strip |= "strip".equalsIgnoreCase(flag);
                    }
                }
                return new RouteDefinition(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()), jwt, strip);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    private record Proxied(Route route, List<HttpClient> clients) {
    }

    private final String targetHost;
    private final int targetPort;
    private final String gatewayPath;
    private final int maxPoolSize;
    private final String routesFile;
    private final JWTParser jwtParser;
    private final ObjectMapper objectMapper;

    private Vertx vertx;
    private Router router;
    private int nextOrder = 0;

    private final ConcurrentMap<String, Proxied> proxies = new ConcurrentHashMap<>();

    public GatewayRoute(
            @ConfigProperty(name = "gateway.target.host") String targetHost,
            @ConfigProperty(name = "gateway.target.port") int targetPort,
            @ConfigProperty(name = "gateway.path", defaultValue = "/") String gatewayPath,
            @ConfigProperty(name = "gateway.maxPoolSize", defaultValue = "20") int maxPoolSize,
            @ConfigProperty(name = "gateway.routes.file", defaultValue = "routes.json") String routesFile,
            JWTParser jwtParser,
            ObjectMapper objectMapper) {
        if (targetHost == null || targetHost.isBlank()) {
            throw new IllegalStateException("'gateway.target.host' es obligatorio");
        }
        if (targetPort <= 0 || targetPort > 65535) {
            throw new IllegalStateException("'gateway.target.port' debe ser un puerto válido");
        }
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.gatewayPath = gatewayPath.isBlank() ? "/" : gatewayPath.trim();
        this.maxPoolSize = maxPoolSize <= 0 ? 20 : maxPoolSize;
        this.routesFile = routesFile;
        this.jwtParser = jwtParser;
        this.objectMapper = objectMapper;
    }

    void init(@Observes Router router, Vertx vertx) {
        this.router = router;
        this.vertx = vertx;

        ConfigProvider.getConfig().getOptionalValue("gateway.routes", String.class).stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(RouteDefinition::parse)
                .filter(Objects::nonNull)
                .forEach(this::addRoute);

        loadManualRoutes().forEach(this::addRoute);

        registerDefaultProxy();
    }

    /** Carga las rutas manuales desde el archivo JSON indicado por 'gateway.routes.file'. */
    private List<RouteDefinition> loadManualRoutes() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(routesFile)) {
            if (in == null) {
                LOG.warnf("No se encontró '%s' en el classpath; no se cargarán rutas manuales", routesFile);
                return Collections.emptyList();
            }
            return objectMapper.readValue(in, new com.fasterxml.jackson.core.type.TypeReference<List<RouteDefinition>>() { });
        } catch (IOException e) {
            LOG.errorf(e, "Error leyendo '%s'; no se cargarán rutas manuales", routesFile);
            return Collections.emptyList();
        }
    }

    private void registerDefaultProxy() {
        HttpClient client = newClient();

        HttpProxy proxy = HttpProxy.reverseProxy(client).origin(targetPort, targetHost);
        proxy.addInterceptor(new ProxyInterceptor() {
            @Override
            public Future<ProxyResponse> handleProxyRequest(ProxyContext context) {
                return context.sendRequest().onFailure(err ->
                        LOG.errorf("Backend por defecto no disponible (%s:%d): %s", targetHost, targetPort, err.getMessage()));
            }
        });

        boolean root = "/".equals(gatewayPath);
        String pattern = root ? "/*" : (gatewayPath.endsWith("/*") ? gatewayPath : gatewayPath + "/*");
        Route route = (root ? router.route() : router.route(pattern))
                .order(DEFAULT_ROUTE_ORDER)
                .handler(rc -> {
                    LOG.warnf("Ruta no registrada: %s %s -> reenviada al backend por defecto (%s:%d)",
                            rc.request().method(), rc.request().path(), targetHost, targetPort);
                    proxy.handle(rc.request());
                });

        proxies.put(DEFAULT_KEY, new Proxied(route, List.of(client)));
        LOG.infof("Gateway activo: %s -> %s:%d", pattern, targetHost, targetPort);
    }

    public synchronized void addRoute(RouteDefinition def) {
        if (router == null) {
            throw new IllegalStateException("Router no inicializado aún");
        }
        if (!def.path().startsWith("/")) {
            LOG.warnf("Ruta inválida ignorada: %s (debe comenzar con '/')", def.path());
            return;
        }
        if (def.targets() == null || def.targets().isEmpty()) {
            LOG.warnf("Ruta sin 'targets' ignorada: %s", def.path());
            return;
        }
        removeRoute(def.path());

        // Un HttpClient + HttpProxy por target: cada backend tiene su propio pool de conexiones,
        // así uno lento o caído no afecta a los demás.
        List<HttpClient> clients = new ArrayList<>();
        List<HttpProxy> targetProxies = new ArrayList<>();
        for (RouteDefinition.Target target : def.targets()) {
            HttpClient client = newClient();
            HttpProxy proxy = HttpProxy.reverseProxy(client).origin(target.port(), target.host());
            proxy.addInterceptor(new ProxyInterceptor() {
                @Override
                public Future<ProxyResponse> handleProxyRequest(ProxyContext context) {
                    if (def.stripPrefix()) {
                        ProxyRequest req = context.request();
                        req.setURI(stripFirstSegment(req.getURI()));
                    }
                    // sendRequest() es lo que intenta conectar de verdad al backend; si el backend
                    // está caído (connection refused, host desconocido, timeout, etc.) el future
                    // falla acá y onFailure nos deja loguearlo sin alterar la respuesta de error
                    // (502/503) que el proxy ya le devuelve al cliente por su cuenta.
                    return context.sendRequest().onFailure(err ->
                            LOG.errorf("Backend no disponible para %s -> %s:%d: %s",
                                    def.path(), target.host(), target.port(), err.getMessage()));
                }
            });
            clients.add(client);
            targetProxies.add(proxy);
        }

        String pattern = def.path().endsWith("/*") ? def.path() : def.path() + "/*";
        Route route = router.route(pattern).order(nextOrder++);
        if (def.requiresJwt()) {
            route.handler(this::requireJwt);
        }
        if (targetProxies.size() == 1) {
            HttpProxy proxy = targetProxies.get(0);
            route.handler(rc -> proxy.handle(rc.request()));
        } else {
            // Round-robin: cada petición que llega toma el siguiente target de la lista, en orden
            // circular. AtomicInteger porque las peticiones pueden llegar concurrentemente.
            AtomicInteger nextTarget = new AtomicInteger();
            route.handler(rc -> {
                int idx = Math.floorMod(nextTarget.getAndIncrement(), targetProxies.size());
                targetProxies.get(idx).handle(rc.request());
            });
        }

        proxies.put(def.path(), new Proxied(route, clients));
        LOG.infof("Ruta agregada: %s -> %s (jwt=%b, stripPrefix=%b)",
                pattern, def.targets(), def.requiresJwt(), def.stripPrefix());
    }

    /**
     * Saca el primer segmento de una URI (path + query string). Ej: /mcsv-auth/auth/login?x=1
     * -> /auth/login?x=1. Si no hay un segundo segmento (ej: /mcsv-auth), deja la raíz "/".
     */
    private static String stripFirstSegment(String uri) {
        int queryIdx = uri.indexOf('?');
        String path = queryIdx < 0 ? uri : uri.substring(0, queryIdx);
        String query = queryIdx < 0 ? "" : uri.substring(queryIdx);
        int secondSlash = path.indexOf('/', 1);
        String rest = secondSlash < 0 ? "/" : path.substring(secondSlash);
        return rest + query;
    }

    /** Exige un JWT válido (firma, emisor y expiración) en el header Authorization. */
    private void requireJwt(RoutingContext rc) {
        String header = rc.request().getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            rc.response().setStatusCode(401).end("Falta el header 'Authorization: Bearer <token>'");
            return;
        }
        try {
            jwtParser.parse(header.substring(7).trim());
            rc.next();
        } catch (ParseException e) {
            LOG.warnf("JWT rechazado en %s: %s", rc.request().path(), e.getMessage());
            rc.response().setStatusCode(401).end("Token inválido o expirado");
        }
    }

    public void addRoute(String path, String host, int port) {
        addRoute(new RouteDefinition(path, host, port));
    }

    public synchronized boolean removeRoute(String path) {
        Proxied p = proxies.remove(path);
        if (p == null) {
            return false;
        }
        try { p.route().remove(); } catch (Exception ignored) { }
        p.clients().forEach(c -> { try { c.close(); } catch (Exception ignored) { } });
        LOG.infof("Ruta removida: %s", path);
        return true;
    }

    private HttpClient newClient() {
        return vertx.createHttpClient(new HttpClientOptions().setMaxPoolSize(maxPoolSize).setKeepAlive(true));
    }

    @PreDestroy
    void shutdown() {
        proxies.keySet().forEach(this::removeRoute);
    }
}
