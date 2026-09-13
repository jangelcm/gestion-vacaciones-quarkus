package com.vacaciones.notificaciones.infraestructura.adaptadores.out.mail;

import com.vacaciones.notificaciones.dominio.model.Adjunto;
import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.port.out.EnviadorEmailPort;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class QuarkusMailerAdapter implements EnviadorEmailPort {

    private static final Logger LOG = Logger.getLogger(QuarkusMailerAdapter.class);

    @Inject
    Mailer mailer;

    @Override
    public void enviar(Destinatario destinatario, String asunto, String cuerpoHtml, Adjunto adjunto) {
        try {
            Mail mail = Mail.withHtml(destinatario.email(), asunto, cuerpoHtml);
            if (adjunto != null) {
                mail.addAttachment(adjunto.nombreArchivo(), adjunto.contenido(), adjunto.contentType());
            }
            mailer.send(mail);
        } catch (RuntimeException e) {
            LOG.errorf(e, "Fallo al enviar email a %s", destinatario.email());
            throw e;
        }
    }
}
