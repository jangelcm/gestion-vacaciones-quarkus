package com.vacaciones.politicas.res;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import com.vacaciones.politicas.dto.response.SaldoDiasResponseDto;
import com.vacaciones.politicas.exception.ResourceNotFoundException;
import com.vacaciones.politicas.service.SaldoDiasService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SaldoDiasResourceTest {

    @InjectMock
    SaldoDiasService saldoDiasService;

    @Test
    void shouldReturnSaldoDiasWhenColaboradorHasRegisteredBalance() {
        when(saldoDiasService.getByColaboradorId(1001L))
                .thenReturn(new SaldoDiasResponseDto(
                        1L,
                        1001L,
                        1L,
                        "10.0",
                        "2.0",
                        "1.0",
                        "15-08-2026 10:00:00",
                        "15-08-2026 10:30:00"));

        given()
        .when()
                .get("/politicas/saldo/1001")
        .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("colaboradorId", equalTo(1001))
                .body("politicaId", equalTo(1))
                .body("diasDisponibles", equalTo("10.0"))
                .body("diasUsados", equalTo("2.0"))
                .body("diasAcumulados", equalTo("1.0"));
    }

    @Test
    void shouldReturnNotFoundWhenColaboradorDoesNotHaveRegisteredBalance() {
        when(saldoDiasService.getByColaboradorId(9999L))
                .thenThrow(new ResourceNotFoundException("Saldo no encontrado para el colaborador"));

        given()
        .when()
                .get("/politicas/saldo/9999")
        .then()
                .statusCode(404)
                .body("mensaje", equalTo("Saldo no encontrado para el colaborador"))
                .body("codeStatus", equalTo("404"));
    }
}