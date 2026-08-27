package org.acme.view;

public record UserRolView(
    Long userId,
    String username,
    Long rolId,
    String rolName
) {

}
