package net.vivans.dcim.module.identity.api.dto;

import net.vivans.dcim.module.identity.domain.model.User;

public record UserResponse(
        String username,
        String role
) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getUsername(), user.getRole());
    }
}
