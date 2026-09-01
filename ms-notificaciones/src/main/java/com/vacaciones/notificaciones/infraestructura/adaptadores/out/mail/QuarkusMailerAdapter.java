package com.vacaciones.notificaciones.infraestructura.adaptadores.out.mail;

import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.port.out.EnviadorEmailPort;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class QuarkusMailerAdapter implements EnviadorEmailPort {

    @Inject
    Mailer mailer;

    @Override
    public void enviar(Destinatario destinatario, String asunto, String cuerpoHtml) {
        mailer.send(Mail.withHtml(destinatario.email(), asunto, cuerpoHtml));
    }
}
