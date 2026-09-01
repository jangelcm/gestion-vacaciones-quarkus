package com.vacaciones.notificaciones.infraestructura.adaptadores.in.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.model.TipoNotificacion;
import com.vacaciones.notificaciones.dominio.port.in.ConsultarHistorialUseCase;
import com.vacaciones.notificaciones.dominio.port.in.EnviarNotificacionUseCase;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
class NotificacionResourceTest {

    @InjectMock
    EnviarNotificacionUseCase enviarNotificacionUseCase;

    @InjectMock
    ConsultarHistorialUseCase consultarHistorialUseCase;

    @Test
    void shouldReturn201AndCallUseCaseWhenRequestIsValid() {
        String body = """
                {
                  "colaboradorId": 1001,
                  "email": "colaborador@empresa.com",
                  "nombre": "Ana Perez",
                  "tipo": "EMAIL",
                  "asunto": "Prueba",
                  "cuerpo": "Cuerpo de prueba"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/notificaciones/test")
        .then()
                .statusCode(201)
                .body("destinatario.colaboradorId", equalTo(1001))
                .body("tipo", equalTo("EMAIL"))
                .body("estado", equalTo("PENDIENTE"));

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(enviarNotificacionUseCase).enviar(captor.capture());
        Notificacion enviada = captor.getValue();
        assertEquals(1001L, enviada.getDestinatario().colaboradorId());
        assertEquals("Prueba", enviada.getAsunto());
        assertNull(enviada.getEventoId());
        assertNull(enviada.getEventoOrigen());
    }

    @Test
    void shouldReturn400WhenTipoIsUnknown() {
        String body = """
                {
                  "colaboradorId": 1001,
                  "email": "colaborador@empresa.com",
                  "nombre": "Ana Perez",
                  "tipo": "SMS",
                  "asunto": "Prueba",
                  "cuerpo": "Cuerpo de prueba"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/notificaciones/test")
        .then()
                .statusCode(400);

        verify(enviarNotificacionUseCase, never()).enviar(any());
    }

    @Test
    void shouldReturnMappedHistorialForColaborador() {
        Notificacion notificacion = new Notificacion(
                "evt-1", TipoNotificacion.EMAIL,
                new Destinatario(1001L, "colaborador@empresa.com", "Ana Perez"),
                "asunto", "cuerpo", "solicitud.aprobada");
        when(consultarHistorialUseCase.consultarPorColaborador(1001L)).thenReturn(List.of(notificacion));

        given()
        .when()
                .get("/notificaciones/historial/1001")
        .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].asunto", equalTo("asunto"))
                .body("[0].estado", equalTo("PENDIENTE"));
    }

    @Test
    void shouldReturnEmptyListWhenNoHistorialForColaborador() {
        when(consultarHistorialUseCase.consultarPorColaborador(9999L)).thenReturn(List.of());

        given()
        .when()
                .get("/notificaciones/historial/9999")
        .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }
}
