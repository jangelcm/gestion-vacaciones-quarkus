package org.acme.dto;

import lombok.Builder;
import java.time.LocalDate;

@Builder
public record UserResponseDto(Long id, String username, String email, String telefono, boolean isActive, LocalDate fechaIngreso) {

}
