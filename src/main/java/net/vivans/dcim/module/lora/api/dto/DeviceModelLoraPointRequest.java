package net.vivans.dcim.module.lora.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceModelLoraPointRequest(
        @NotBlank String payloadField,
        @NotBlank String pointName,
        Integer dataPointTypeId,
        @Schema(description = "UNIT 그룹 common_code.id (단위가 없으면 null)", example = "58")
        Integer unitCodeId,
        Double scale,
        String valueMap,
        Boolean enabled
) {
}
