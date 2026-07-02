package net.vivans.dcim.module.location.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.location.api.dto.LocationNodeCreateRequest;
import net.vivans.dcim.module.location.api.dto.LocationNodeParentUpdateRequest;
import net.vivans.dcim.module.location.api.dto.LocationNodeResponse;
import net.vivans.dcim.module.location.api.dto.LocationNodeUpdateRequest;
import net.vivans.dcim.module.location.application.LocationNodeQueryService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/location-node")
@RequiredArgsConstructor
@Tag(name = "location-node", description = "위치 노드 관리 API")
public class LocationNodeController {

    private final LocationNodeQueryService nodeQueryService;

    @PostMapping
    @Operation(summary = "위치 노드 등록 API", description = "code는 서버에서 10자 Base62 문자열로 자동 생성됩니다.")
    public ResponseEntity<ApiResponse<LocationNodeResponse>> createLocationNode(
            @Valid @RequestBody LocationNodeCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(nodeQueryService.createLocationNode(request)));
    }

    @PutMapping("/{code}")
    @Operation(summary = "위치 노드 수정 API", description = "locationType, name만 수정 가능합니다. code와 parent는 변경할 수 없습니다.")
    public ResponseEntity<ApiResponse<LocationNodeResponse>> updateLocationNode(
            @Parameter(description = "위치 노드 code") @PathVariable String code,
            @Valid @RequestBody LocationNodeUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(nodeQueryService.updateLocationNode(code, request)));
    }

    @PatchMapping("/{code}/parent")
    @Operation(summary = "상위 노드 변경 API", description = "parentCode가 null이거나 비어 있으면 루트로 승격합니다.")
    public ResponseEntity<ApiResponse<LocationNodeResponse>> updateParentLocationNode(
            @Parameter(description = "위치 노드 code") @PathVariable String code,
            @Valid @RequestBody LocationNodeParentUpdateRequest request){
        return ResponseEntity.ok(ApiResponse.ok(nodeQueryService.updateParentLocationNode(code, request)));
    }
}
