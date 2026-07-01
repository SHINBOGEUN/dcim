package net.vivans.dcim.module.location.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.location.api.dto.LocationNodeRequest;
import net.vivans.dcim.module.location.api.dto.LocationNodeResponse;
import net.vivans.dcim.module.location.application.LocationNodeQueryService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/location-node")
@RequiredArgsConstructor
@Tag(name = "location-node", description = "위치 노드 관리 API")
public class LocationNodeController {

    private final LocationNodeQueryService nodeQueryService;

    @PostMapping
    @Operation(summary = "위치 노드 등록 API")
    public ResponseEntity<ApiResponse<LocationNodeResponse>> createLocationNode(
            @Valid @RequestBody LocationNodeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(nodeQueryService.createLocationNode(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "위치 노드 수정 API")
    public ResponseEntity<ApiResponse<LocationNodeResponse>> updateLocationNode(
            @Parameter(description = "위치 노드 ID") @PathVariable Integer id,
            @Valid @RequestBody LocationNodeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(nodeQueryService.updateLocationNode(id, request)));
    }
}
