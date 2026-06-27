package net.vivans.dcim.module.common.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record CodeGroupRequest(
        @Schema(description = "코드 그룹 키", example = "DEVICE_TYPE")
        @NotEmpty(message = "groupKey must not be empty")
        String groupKey,

        @Schema(description = "코드 그룹 이름", example = "장비 유형")
        @NotEmpty(message = "groupName must not be empty")
        String groupName
) {
}
