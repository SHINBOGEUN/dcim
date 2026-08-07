package net.vivans.dcim.module.device.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.device.api.dto.DeviceSnmpInstanceCreateRequest;
import net.vivans.dcim.module.device.api.dto.DeviceSnmpInstanceResponse;
import net.vivans.dcim.module.device.application.DeviceSnmpInstanceQueryService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/devices/{deviceId}/endpoints/{endpointId}/snmp-instance")
@Tag(name = "device-snmp-instance", description = "장비 SNMP instance 인덱스 API")
public class DeviceSnmpInstanceController {

    private final DeviceSnmpInstanceQueryService deviceSnmpInstanceQueryService;

    @PostMapping
    @Operation(summary = "SNMP instance 등록 API",
            description = "SNMP endpoint당 1건. OID {instanceId} 치환용. 모델에 requiresInstance point가 있을 때만.")
    public ResponseEntity<ApiResponse<DeviceSnmpInstanceResponse>> createSnmpInstance(
            @Parameter(description = "장비 ID") @PathVariable Integer deviceId,
            @Parameter(description = "엔드포인트 ID") @PathVariable Integer endpointId,
            @Valid @RequestBody DeviceSnmpInstanceCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(deviceSnmpInstanceQueryService.createSnmpInstance(deviceId, endpointId, request)));
    }
}
