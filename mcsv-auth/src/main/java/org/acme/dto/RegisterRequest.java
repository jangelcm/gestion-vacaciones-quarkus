package org.acme.dto;

import java.time.LocalDate;

public class RegisterRequest {
    public String username;
    public String password;
    public String email;
    public String rol;
    public LocalDate fechaIngreso;

    public RegisterRequest() {}
}
