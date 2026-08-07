package net.vivans.dcim.module.device.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DeviceSnmpInstanceCreateRequest(
        @Schema(description = "SNMP MIB instance index ({instanceId} 치환값)", example = "1")
        @NotNull(message = "instanceId is required")
        @Min(value = 1, message = "instanceId must be greater than or equal to 1")
        Integer instanceId
) {
}
