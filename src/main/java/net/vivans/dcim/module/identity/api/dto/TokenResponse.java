package net.vivans.dcim.module.identity.api.dto;

import net.vivans.dcim.module.identity.domain.model.User;

public record TokenResponse(
        String username,
        String role,
        String accessToken,
        String refreshToken
) {

    public static TokenResponse of(User user, String accessToken, String refreshToken) {
        return new TokenResponse(user.getUsername(), user.getRole(), accessToken, refreshToken);
    }
}
