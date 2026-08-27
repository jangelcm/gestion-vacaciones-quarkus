package com.vacaciones.politicas.res;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.vacaciones.politicas.dto.response.ValidarSolicitudResponseDto;
import com.vacaciones.politicas.service.ValidacionService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ValidacionResourceTest {

    @InjectMock
    ValidacionService validacionService;

    @Test
    void shouldReturnApprovedResponseWhenSolicitudIsValid() {
        when(validacionService.validarSolicitud(any(), eq(12)))
                .thenReturn(new ValidarSolicitudResponseDto(true, 5, null));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "colaboradorId": 1001,
                          "fechaInicio": "2026-08-17",
                          "fechaFin": "2026-08-21",
                          "tipoVacacion": "ANUAL",
                          "antiguedadMeses": 12
                        }
                        """)
        .when()
                .post("/api/v1/politicas/validar")
        .then()
                .statusCode(200)
                .body("aprobado", equalTo(true))
                .body("diasSolicitados", equalTo(5))
                .body("motivoRechazo", equalTo(null));
    }

    @Test
    void shouldReturnRejectedResponseWhenSaldoIsInsufficient() {
        when(validacionService.validarSolicitud(any(), eq(12)))
                .thenReturn(new ValidarSolicitudResponseDto(false, 5, "Saldo insuficiente para la solicitud"));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "colaboradorId": 1002,
                          "fechaInicio": "2026-08-17",
                          "fechaFin": "2026-08-21",
                          "tipoVacacion": "ANUAL",
                          "antiguedadMeses": 12
                        }
                        """)
        .when()
                .post("/api/v1/politicas/validar")
        .then()
                .statusCode(200)
                .body("aprobado", equalTo(false))
                .body("diasSolicitados", equalTo(5))
                .body("motivoRechazo", equalTo("Saldo insuficiente para la solicitud"));
    }

    @Test
    void shouldReturnRejectedResponseWhenMinimumSeniorityIsNotMet() {
        when(validacionService.validarSolicitud(any(), eq(6)))
                .thenReturn(new ValidarSolicitudResponseDto(false, 5, "No cumple con la antiguedad minima requerida"));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "colaboradorId": 1003,
                          "fechaInicio": "2026-08-17",
                          "fechaFin": "2026-08-21",
                          "tipoVacacion": "ANUAL",
                          "antiguedadMeses": 6
                        }
                        """)
        .when()
                .post("/api/v1/politicas/validar")
        .then()
                .statusCode(200)
                .body("aprobado", equalTo(false))
                .body("diasSolicitados", equalTo(5))
                .body("motivoRechazo", equalTo("No cumple con la antiguedad minima requerida"));
    }
}