package com.vacaciones.notificaciones.testsupport;

import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.port.in.EnviarNotificacionUseCase;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import org.mockito.Mockito;

/**
 * @InjectMock no logra interceptar la inyeccion por constructor en los
 * consumers @Incoming en esta version de Quarkus (probado, falla incluso
 * en aislamiento). Este bean @Mock delega a un mock de Mockito compartido,
 * que los tests pueden verificar/resetear directamente.
 */
@Mock
@ApplicationScoped
public class EnviarNotificacionUseCaseMock implements EnviarNotificacionUseCase {

    public static final EnviarNotificacionUseCase DELEGATE = Mockito.mock(EnviarNotificacionUseCase.class);

    @Override
    public void enviar(Notificacion notificacion) {
        DELEGATE.enviar(notificacion);
    }
}
