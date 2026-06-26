package net.vivans.dcim.module.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record AuthRequest(
        @Schema(description = "회원 아이디 혹은 이메일", example = "test")
        @NotEmpty(message = "username must not be null")
        String username,

        @Schema(description = "회원 비밀번호", example = "test")
        @NotEmpty(message = "password must not be null")
        String password
) {

    @Override
    public String toString() {
        return "AuthRequest{username='" + username + "', password='[HIDDEN]'}";
    }
}
