package com.vacaciones.notificaciones.dominio.port.out;

import com.vacaciones.notificaciones.dominio.model.Destinatario;

public interface EnviadorEmailPort {

    void enviar(Destinatario destinatario, String asunto, String cuerpoHtml);
}
