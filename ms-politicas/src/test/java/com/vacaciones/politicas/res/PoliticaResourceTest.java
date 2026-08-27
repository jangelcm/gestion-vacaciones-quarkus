package com.vacaciones.politicas.res;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.vacaciones.politicas.dto.response.PoliticaResponseDto;
import com.vacaciones.politicas.service.PoliticaService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PoliticaResourceTest {

    @InjectMock
    PoliticaService politicaService;

    @Test
    void shouldCreatePolitica() {
        when(politicaService.save(any()))
                .thenReturn(buildResponse(1L, "Politica temporal QA", 15, 30));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nombre": "Politica temporal QA",
                          "tipoVacacion": "ANUAL",
                          "diasBaseAnio": 15,
                          "antiguedadMinimaMeses": 12,
                          "acumulable": true,
                          "maxDiasAcumulables": 30,
                          "activa": true
                        }
                        """)
        .when()
                .post("/api/v1/politicas")
        .then()
                .statusCode(201)
                .body("id", equalTo(1))
                .body("nombre", equalTo("Politica temporal QA"))
                .body("diasBaseAnio", equalTo(15));
    }

    @Test
    void shouldReturnPoliticaById() {
        when(politicaService.findById(1L))
                .thenReturn(buildResponse(1L, "Politica anual", 15, 30));

        given()
        .when()
                .get("/api/v1/politicas/1")
        .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("nombre", equalTo("Politica anual"))
                .body("tipoVacacion", equalTo("ANUAL"));
    }

    @Test
    void shouldUpdatePolitica() {
        when(politicaService.update(eq(1L), any()))
                .thenReturn(buildResponse(1L, "Politica anual actualizada", 20, 35));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nombre": "Politica anual actualizada",
                          "tipoVacacion": "ANUAL",
                          "diasBaseAnio": 20,
                          "antiguedadMinimaMeses": 12,
                          "acumulable": true,
                          "maxDiasAcumulables": 35,
                          "activa": true
                        }
                        """)
        .when()
                .put("/api/v1/politicas/1")
        .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("nombre", equalTo("Politica anual actualizada"))
                .body("diasBaseAnio", equalTo(20))
                .body("maxDiasAcumulables", equalTo(35));
    }

    @Test
    void shouldReturnAllPoliticas() {
        when(politicaService.getAll())
                .thenReturn(List.of(
                        buildResponse(1L, "Politica anual", 15, 30),
                        buildResponse(2L, "Politica premium", 20, 40)));

        given()
        .when()
                .get("/api/v1/politicas")
        .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].id", equalTo(1))
                .body("[1].id", equalTo(2));
    }

    @Test
    void shouldDeletePolitica() {
        when(politicaService.delete(1L))
                .thenReturn(buildResponse(1L, "Politica anual", 15, 30));

        given()
        .when()
                .delete("/api/v1/politicas/1")
        .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("nombre", equalTo("Politica anual"));
    }

    private PoliticaResponseDto buildResponse(Long id, String nombre, Integer diasBaseAnio, Integer maxDiasAcumulables) {
        return new PoliticaResponseDto(
                id,
                nombre,
                "ANUAL",
                diasBaseAnio,
                12,
                true,
                maxDiasAcumulables,
                true,
                "21-08-2026 10:00:00",
                "21-08-2026 10:30:00");
    }
}