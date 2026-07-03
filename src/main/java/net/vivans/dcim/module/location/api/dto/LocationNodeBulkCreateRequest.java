package net.vivans.dcim.module.location.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LocationNodeBulkCreateRequest(
        @Schema(description = "기존 부모 노드 code (null이면 nodes가 루트로 등록)", example = "K7mN2pQx9L")
        String parentCode,

        @Schema(description = "등록할 노드 트리 목록 (부모 → 자식 순으로 처리)")
        @NotEmpty(message = "nodes must not be empty")
        @Valid
        List<LocationNodeTreeCreateRequest> nodes
) {
}
