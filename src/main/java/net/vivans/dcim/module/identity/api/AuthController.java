package net.vivans.dcim.module.identity.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.identity.api.dto.AuthRequest;
import net.vivans.dcim.module.identity.api.dto.TokenResponse;
import net.vivans.dcim.module.identity.api.dto.UserResponse;
import net.vivans.dcim.module.identity.application.AuthCommandService;
import net.vivans.dcim.module.identity.application.AuthQueryService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/auth")
@Tag(name = "auth", description = "인증 관련 API")
public class AuthController {

    private final AuthCommandService authCommandService;
    private final AuthQueryService authQueryService;

    @Operation(summary = "회원 아이디와 비밀번호를 입력받아 토큰을 발급하는 API")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody AuthRequest request) {
        TokenResponse token = authCommandService.login(request.username(), request.password());
        return ResponseEntity.ok(ApiResponse.ok(token));
    }

    @Operation(summary = "회원 아이디와 비밀번호를 입력받아 신규 회원을 추가하는 API")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody AuthRequest request) {
        UserResponse user = authCommandService.register(request.username(), request.password());
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @Operation(summary = "리프레시 토큰으로 새로운 토큰을 발급받는 API")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Parameter(required = true) @RequestParam String refreshToken
    ) {
        TokenResponse token = authCommandService.refresh(refreshToken);
        return ResponseEntity.ok(ApiResponse.ok(token));
    }

    @Operation(summary = "엑세스 토큰 검증을 위한 API")
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<TokenResponse>> validate(
            @Parameter(required = true) @RequestParam String accessToken
    ) {
        TokenResponse token = authQueryService.validate(accessToken);
        return ResponseEntity.ok(ApiResponse.ok(token));
    }
}
