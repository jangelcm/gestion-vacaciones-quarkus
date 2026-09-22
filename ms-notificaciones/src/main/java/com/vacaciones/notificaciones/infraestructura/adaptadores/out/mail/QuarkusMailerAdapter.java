package com.vacaciones.notificaciones.infraestructura.adaptadores.out.mail;

import com.vacaciones.notificaciones.dominio.model.Adjunto;
import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.port.out.EnviadorEmailPort;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class QuarkusMailerAdapter implements EnviadorEmailPort {

    private static final Logger LOG = Logger.getLogger(QuarkusMailerAdapter.class);

    @Inject
    ReactiveMailer mailer;

    /**
     * Envio "fire-and-forget": no bloquea el hilo que procesa el mensaje de Kafka esperando al
     * SMTP (en modo nativo, el establecimiento de la conexion TCP+TLS con Gmail puede tardar
     * varias decenas de segundos, muy por encima del timeout razonable para no bloquear el
     * commit del consumer de Kafka). El resultado real (exito o fallo) se loguea cuando el envio
     * efectivamente termina, sin carrera contra ningun timeout — evita el falso negativo que se
     * daba antes al usar el Mailer bloqueante con Uni.await().atMost(...): ese await() dejaba de
     * esperar al vencer el timeout, pero la conexion real seguia corriendo en background y
     * terminaba enviando el correo igual, ya sin que la app se enterara.
     */
    @Override
    public void enviar(Destinatario destinatario, String asunto, String cuerpoHtml, Adjunto adjunto) {
        Mail mail = Mail.withHtml(destinatario.email(), asunto, cuerpoHtml);
        if (adjunto != null) {
            mail.addAttachment(adjunto.nombreArchivo(), adjunto.contenido(), adjunto.contentType());
        }
        mailer.send(mail)
                .subscribe().with(
                        ignored -> LOG.infof("Email enviado a %s", destinatario.email()),
                        failure -> LOG.errorf(failure, "Fallo al enviar email a %s", destinatario.email()));
    }
}
