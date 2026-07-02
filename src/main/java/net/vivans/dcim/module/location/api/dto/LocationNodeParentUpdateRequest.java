package net.vivans.dcim.module.location.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LocationNodeParentUpdateRequest(
        @Schema(description = "새 부모 code, null이면 루트")
        String parentCode
) {}