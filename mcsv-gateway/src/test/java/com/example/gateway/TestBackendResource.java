package com.example.gateway;

import com.sun.net.httpserver.HttpServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Backend HTTP mínimo (JDK com.sun.net.httpserver) usado solo en tests
 * para verificar que GatewayRoute reenvía correctamente las peticiones.
 */
public class TestBackendResource implements QuarkusTestResourceLifecycleManager {

    private HttpServer server;

    // 8081 es el test-port por defecto de Quarkus (quarkus.http.test-port), así que el
    // backend falso debe usar otro puerto para no chocar con la propia app bajo test.
    private static final int BACKEND_PORT = 8082;

    @Override
    public Map<String, String> start() {
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", BACKEND_PORT), 0);
            server.createContext("/", exchange -> {
                byte[] body = "hello from backend".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Map.of("gateway.target.port", String.valueOf(BACKEND_PORT));
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}
