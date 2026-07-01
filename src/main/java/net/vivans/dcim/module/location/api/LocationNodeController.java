package net.vivans.dcim.module.location.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.location.api.dto.LocationNodeRequest;
import net.vivans.dcim.module.location.api.dto.LocationNodeResponse;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/location-node")
@RequiredArgsConstructor
@Tag(name = "location-node", description = "위치 노드 관리 API")
public class LocationNodeController {

    @PostMapping
    @Operation(summary = "위치 노드 등록 API")
    public ResponseEntity<ApiResponse<LocationNodeResponse>> createLocationNode(LocationNodeRequest request){
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
