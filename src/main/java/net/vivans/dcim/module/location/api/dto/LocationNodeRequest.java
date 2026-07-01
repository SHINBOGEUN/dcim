package net.vivans.dcim.module.location.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LocationNodeRequest(
        @Schema(description = "상위 노드 ID (루트인 경우 null)", example = "1")
        Integer parentId,

        @Schema(description = "위치 유형 ID (LOCATION_TYPE 그룹)", example = "1")
        @NotNull(message = "locationTypeId must not be null")
        Integer locationTypeId,

        @Schema(description = "노드 이름", example = "서울 DC")
        @NotBlank(message = "name must not be empty")
        String name,

        @Schema(description = "노드 코드", example = "SEOUL-DC")
        String code
) {
}
