package net.vivans.dcim.module.device.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.device.api.dto.DeviceCreateRequest;
import net.vivans.dcim.module.device.api.dto.DeviceResponse;
import net.vivans.dcim.module.device.application.DeviceQueryService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/devices")
@Tag(name = "device", description = "DCIM 장비 CRUD API")
public class DeviceController {

    private final DeviceQueryService deviceQueryService;

    @PostMapping
    @Operation(summary = "장비 등록 API", description = "위치를 아직 모를 경우 locationNodeCode에 UNASSIGNED를 지정합니다.")
    public ResponseEntity<ApiResponse<DeviceResponse>> createDevice(
            @Valid @RequestBody DeviceCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(deviceQueryService.createDevice(request)));
    }
}
