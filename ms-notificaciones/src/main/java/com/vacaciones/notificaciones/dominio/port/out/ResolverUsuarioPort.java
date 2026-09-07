package com.vacaciones.notificaciones.dominio.port.out;

import com.vacaciones.notificaciones.dominio.model.UsuarioInfo;
import java.util.List;
import java.util.Optional;

public interface ResolverUsuarioPort {

    Optional<UsuarioInfo> resolverPorColaboradorId(Long colaboradorId);

    List<UsuarioInfo> resolverPorRol(String rol);
}
