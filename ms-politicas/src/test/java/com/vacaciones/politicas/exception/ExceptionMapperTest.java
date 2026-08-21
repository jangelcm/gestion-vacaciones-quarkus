package com.vacaciones.politicas.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExceptionMapperTest {

    @Mock
    UriInfo uriInfo;

    private ExceptionMapper exceptionMapper;

    @BeforeEach
    void setUp() {
        exceptionMapper = new ExceptionMapper();
        exceptionMapper.setUriInfo(uriInfo);
        URI uri = UriBuilder.fromPath("/politicas/validar").build();
        when(uriInfo.getRequestUri()).thenReturn(uri);
    }

    @Test
    void shouldMapRuntimeCustomExceptionToInternalServerError() {
        Response response = exceptionMapper.toResponse(new RuntimeCustomException("error interno"));

        assertResponse(response, 500, "error interno");
    }

    @Test
    void shouldMapBadRequestExceptionToBadRequest() {
        Response response = exceptionMapper.toResponse(new BadRequestException("request invalido"));

        assertResponse(response, 400, "request invalido");
    }

    @Test
    void shouldMapResourceNotFoundExceptionToNotFound() {
        Response response = exceptionMapper.toResponse(new ResourceNotFoundException("recurso no encontrado"));

        assertResponse(response, 404, "recurso no encontrado");
    }

    @Test
    void shouldMapResourceUnAuthorizedExceptionToUnauthorized() {
        Response response = exceptionMapper.toResponse(new ResourceUnAuthorizedException("no autorizado"));

        assertResponse(response, 401, "no autorizado");
    }

    @Test
    void shouldMapRequestValidationExceptionToBadRequest() {
        Response response = exceptionMapper.toResponse(new RequestValidationException("validacion fallida"));

        assertResponse(response, 400, "validacion fallida");
    }

    @Test
    void shouldMapSaldoInsuficienteExceptionToBadRequest() {
        Response response = exceptionMapper.toResponse(new SaldoInsuficienteException("saldo insuficiente"));

        assertResponse(response, 400, "saldo insuficiente");
    }

    private void assertResponse(Response response, int expectedStatus, String expectedMessage) {
        assertEquals(expectedStatus, response.getStatus());
        Object entity = response.getEntity();
        assertNotNull(entity);

        ErrorResponse errorResponse = (ErrorResponse) entity;
        assertNotNull(errorResponse.hora());
        assertEquals(expectedMessage, errorResponse.mensaje());
        assertEquals("/politicas/validar", errorResponse.url());
        assertEquals(String.valueOf(expectedStatus), errorResponse.codeStatus());
    }
}