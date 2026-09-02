package com.vacaciones.notificaciones.dominio.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DestinatarioTest {

    @Test
    void shouldCreateDestinatarioWhenDataIsValid() {
        Destinatario destinatario = assertDoesNotThrow(
                () -> new Destinatario(1001L, "colaborador@empresa.com", "Ana Perez"));

        assertEquals(1001L, destinatario.colaboradorId());
        assertEquals("colaborador@empresa.com", destinatario.email());
        assertEquals("Ana Perez", destinatario.nombre());
    }

    @Test
    void shouldThrowWhenColaboradorIdIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Destinatario(null, "colaborador@empresa.com", "Ana Perez"));
        assertEquals("colaboradorId no puede ser null", ex.getMessage());
    }

    @Test
    void shouldThrowWhenEmailIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new Destinatario(1001L, null, "Ana Perez"));
    }

    @Test
    void shouldThrowWhenEmailIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> new Destinatario(1001L, "", "Ana Perez"));
    }

    @Test
    void shouldThrowWhenEmailHasNoAtSymbol() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Destinatario(1001L, "colaborador.empresa.com", "Ana Perez"));
        assertEquals("email invalido: colaborador.empresa.com", ex.getMessage());
    }

    @Test
    void shouldThrowWhenNombreIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new Destinatario(1001L, "colaborador@empresa.com", null));
    }

    @Test
    void shouldThrowWhenNombreIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> new Destinatario(1001L, "colaborador@empresa.com", ""));
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Destinatario primero = new Destinatario(1001L, "colaborador@empresa.com", "Ana Perez");
        Destinatario segundo = new Destinatario(1001L, "colaborador@empresa.com", "Ana Perez");

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }
}
