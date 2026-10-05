package net.vivans.dcim.module.device.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.device.api.dto.DevicePageModelPointSettingsRequest;
import net.vivans.dcim.module.device.api.dto.DevicePageModelPointSettingsResponse;
import net.vivans.dcim.module.device.application.DevicePageModelPointSettingService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/device-pages")
@Tag(name = "device-page-point-settings", description = "페이지별 모델 측정항목 표시 설정 API")
public class DevicePageModelPointSettingController {
    private final DevicePageModelPointSettingService service;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{pageCode}/models/{modelId}/point-settings")
    @Operation(summary = "페이지·모델별 측정항목 표시 설정 조회",
            description = "모델 카탈로그의 모든 포인트와 표시 여부를 반환합니다. 설정이 없으면 숨김으로 반환합니다.")
    public ResponseEntity<ApiResponse<DevicePageModelPointSettingsResponse>> getSettings(
            @Parameter(description = "DEVICE_PAGE 그룹의 코드") @PathVariable String pageCode,
            @Parameter(description = "device_model.id") @PathVariable Integer modelId) {
        return ResponseEntity.ok(ApiResponse.ok(service.getSettings(pageCode, modelId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{pageCode}/models/{modelId}/point-settings")
    @Operation(summary = "페이지·모델별 측정항목 표시 설정 전체 교체",
            description = "요청에 빠진 포인트는 숨김 처리합니다. pointId는 protocolTypeId와 함께 식별됩니다.")
    public ResponseEntity<ApiResponse<DevicePageModelPointSettingsResponse>> replaceSettings(
            @Parameter(description = "DEVICE_PAGE 그룹의 코드") @PathVariable String pageCode,
            @Parameter(description = "device_model.id") @PathVariable Integer modelId,
            @Valid @RequestBody DevicePageModelPointSettingsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.replaceSettings(pageCode, modelId, request)));
    }
}
