package net.vivans.dcim.module.device.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceProtocolEndpointCreateRequest(
        @Schema(description = "PROTOCOL_TYPE common_code ID", example = "9")
        @NotNull(message = "protocolTypeId is required")
        Integer protocolTypeId,

        @Schema(description = "IP 또는 hostname", example = "192.168.1.10")
        @NotBlank(message = "host must not be empty")
        String host,

        @Schema(description = "포트 (1~65535)", example = "161")
        @NotNull(message = "port is required")
        @Min(value = 1, message = "port must be between 1 and 65535")
        @Max(value = 65535, message = "port must be between 1 and 65535")
        Integer port,

        @Schema(description = "사용 여부 (기본 true)", example = "true")
        Boolean enabled
) {
}
