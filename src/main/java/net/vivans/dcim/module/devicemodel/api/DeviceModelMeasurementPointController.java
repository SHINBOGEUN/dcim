package net.vivans.dcim.module.devicemodel.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.devicemodel.api.dto.DeviceModelMeasurementPointOptionsResponse;
import net.vivans.dcim.module.devicemodel.application.DeviceModelMeasurementPointQueryService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/device-models")
@Tag(name = "device-model-measurements", description = "모델별 측정항목 카탈로그 API")
public class DeviceModelMeasurementPointController {
    private final DeviceModelMeasurementPointQueryService queryService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{modelId}/measurement-points")
    @Operation(summary = "모델의 페이지 설정용 측정항목 조회",
            description = "모델에 등록된 enabled SNMP·Modbus·LoRa 측정항목을 반환합니다. 장비 인스턴스와 페이지 배정은 필요하지 않습니다.")
    public ResponseEntity<ApiResponse<DeviceModelMeasurementPointOptionsResponse>> getMeasurementPoints(
            @Parameter(description = "device_model.id") @PathVariable Integer modelId) {
        return ResponseEntity.ok(ApiResponse.ok(queryService.getMeasurementPoints(modelId)));
    }
}
