package com.vacaciones.politicas.res;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.vacaciones.politicas.dto.response.SaldoDiasResponseDto;
import com.vacaciones.politicas.exception.ResourceNotFoundException;
import com.vacaciones.politicas.exception.RuntimeCustomException;
import com.vacaciones.politicas.service.SaldoDiasService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import java.util.List;
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
                .get("/api/v1/politicas/saldo/1001")
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
                .get("/api/v1/politicas/saldo/9999")
        .then()
                .statusCode(404)
                .body("mensaje", equalTo("Saldo no encontrado para el colaborador"))
                .body("codeStatus", equalTo("404"));
    }

    @Test
    void shouldAssignPoliticaToColaboradorAndReturnCreated() {
        doNothing().when(saldoDiasService).asignarPolitica(1001L, 1L);

        given()
        .when()
                .post("/api/v1/politicas/1/colaboradores/1001")
        .then()
                .statusCode(201);
    }

    @Test
    void shouldReturnConflictWhenColaboradorAlreadyHasAssignedPolitica() {
        doThrow(new RuntimeCustomException(
                "El colaborador ya tiene una politica asignada",
                Response.Status.CONFLICT))
                .when(saldoDiasService).asignarPolitica(1001L, 1L);

        given()
        .when()
                .post("/api/v1/politicas/1/colaboradores/1001")
        .then()
                .statusCode(409)
                .body("mensaje", equalTo("El colaborador ya tiene una politica asignada"))
                .body("codeStatus", equalTo("409"));
    }

    @Test
    void shouldReturnSaldoDiasListByPoliticaId() {
        when(saldoDiasService.getByPoliticaId(1L))
                .thenReturn(List.of(
                        new SaldoDiasResponseDto(
                                1L,
                                1001L,
                                1L,
                                "15.0",
                                "0.0",
                                "0.0",
                                "21-08-2026 10:00:00",
                                "21-08-2026 10:00:00"),
                        new SaldoDiasResponseDto(
                                2L,
                                1002L,
                                1L,
                                "12.0",
                                "3.0",
                                "1.0",
                                "21-08-2026 11:00:00",
                                "21-08-2026 11:00:00")));

        given()
        .when()
                .get("/api/v1/politicas/1/colaboradores")
        .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].colaboradorId", equalTo(1001))
                .body("[0].politicaId", equalTo(1))
                .body("[1].colaboradorId", equalTo(1002))
                .body("[1].politicaId", equalTo(1));
    }
}