package net.vivans.dcim.module.device.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.device.api.dto.DeviceProtocolEndpointCreateRequest;
import net.vivans.dcim.module.device.api.dto.DeviceProtocolEndpointResponse;
import net.vivans.dcim.module.device.application.DeviceProtocolEndpointQueryService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/devices/{deviceId}/endpoints")
@Tag(name = "device-protocol-endpoint", description = "장비 프로토콜 엔드포인트 CRUD API")
public class DeviceProtocolEndpointController {

    private final DeviceProtocolEndpointQueryService deviceProtocolEndpointQueryService;

    @PostMapping
    @Operation(summary = "프로토콜 엔드포인트 등록 API", description = "장비당 프로토콜 타입 1건. host/port 공통 전송층.")
    public ResponseEntity<ApiResponse<DeviceProtocolEndpointResponse>> createEndpoint(
            @Parameter(description = "장비 ID") @PathVariable Integer deviceId,
            @Valid @RequestBody DeviceProtocolEndpointCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                deviceProtocolEndpointQueryService.createEndpoint(deviceId, request)));
    }

    @PutMapping("/{endpointId}")
    @Operation(summary = "프로토콜 엔드포인트 수정 API", description = "요청 body는 등록과 동일하며 전체 교체입니다.")
    public ResponseEntity<ApiResponse<DeviceProtocolEndpointResponse>> updateEndpoint(
            @Parameter(description = "장비 ID") @PathVariable Integer deviceId,
            @Parameter(description = "엔드포인트 ID") @PathVariable Integer endpointId,
            @Valid @RequestBody DeviceProtocolEndpointCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                deviceProtocolEndpointQueryService.updateEndpoint(deviceId, endpointId, request)));
    }
}


