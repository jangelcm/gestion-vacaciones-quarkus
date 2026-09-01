package com.vacaciones.notificaciones.infraestructura.adaptadores.out.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vacaciones.notificaciones.dominio.model.Destinatario;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class QuarkusMailerAdapterTest {

    private static final Destinatario DESTINATARIO =
            new Destinatario(1001L, "colaborador@empresa.com", "Ana Perez");

    @Inject
    QuarkusMailerAdapter adapter;

    @Inject
    MockMailbox mailbox;

    @BeforeEach
    void limpiarBuzon() {
        mailbox.clear();
    }

    @Test
    void shouldCaptureExactlyOneEmailWithCorrectDestinatarioAsuntoYCuerpo() {
        adapter.enviar(DESTINATARIO, "Solicitud aprobada", "<p>Tu solicitud fue aprobada</p>");

        List<Mail> enviados = mailbox.getMailsSentTo("colaborador@empresa.com");

        assertEquals(1, enviados.size());
        Mail correo = enviados.get(0);
        assertTrue(correo.getTo().contains("colaborador@empresa.com"));
        assertEquals("Solicitud aprobada", correo.getSubject());
        assertEquals("<p>Tu solicitud fue aprobada</p>", correo.getHtml());
    }
}
