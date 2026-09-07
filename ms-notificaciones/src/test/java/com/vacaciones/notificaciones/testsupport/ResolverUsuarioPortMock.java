package com.vacaciones.notificaciones.testsupport;

import com.vacaciones.notificaciones.dominio.model.UsuarioInfo;
import com.vacaciones.notificaciones.dominio.port.out.ResolverUsuarioPort;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import org.mockito.Mockito;

/**
 * Mismo patron que {@link EnviarNotificacionUseCaseMock}: @InjectMock no intercepta la
 * inyeccion por constructor en los consumers @Incoming, asi que este bean @Mock delega
 * a un mock de Mockito compartido que los tests pueden stubear/verificar directamente.
 */
@Mock
@ApplicationScoped
public class ResolverUsuarioPortMock implements ResolverUsuarioPort {

    public static final ResolverUsuarioPort DELEGATE = Mockito.mock(ResolverUsuarioPort.class);

    @Override
    public Optional<UsuarioInfo> resolverPorColaboradorId(Long colaboradorId) {
        return DELEGATE.resolverPorColaboradorId(colaboradorId);
    }

    @Override
    public List<UsuarioInfo> resolverPorRol(String rol) {
        return DELEGATE.resolverPorRol(rol);
    }
}
