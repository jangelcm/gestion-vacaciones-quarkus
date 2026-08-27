package org.acme.dto;

import lombok.Builder;

@Builder
public record UserResponseDto(Long id, String username, String email, String telefono, boolean isActive) {

}
