package net.vivans.dcim.module.devicemodel.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record DeviceModelProtocolRequest(
        @Schema(description = "PROTOCOL_TYPE common_code ID", example = "7")
        @NotNull(message = "protocolTypeId must not be null")
        Integer protocolTypeId
) {
}
