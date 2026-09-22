package org.acme.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import java.time.LocalDate;

@RegisterForReflection
@Builder
public record UserResponseDto(Long id, String username, String email, String telefono, boolean isActive, LocalDate fechaIngreso) {

}
