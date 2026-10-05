package net.vivans.dcim.module.device.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.device.api.dto.DevicePageOptionsResponse;
import net.vivans.dcim.module.device.application.DevicePageOptionsQueryService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/device-pages")
@Tag(name = "device-page-options", description = "페이지별 장비 및 측정 포인트 선택 옵션 API")
public class DevicePageOptionsController {
    private final DevicePageOptionsQueryService queryService;

    @GetMapping("/{pageCode}/options")
    @Operation(summary = "페이지의 장비 및 표시 가능 포인트 옵션 조회",
            description = "device_page_device에 연결된 장비와, 해당 모델에서 페이지 노출로 설정된 포인트를 반환합니다.")
    public ResponseEntity<ApiResponse<DevicePageOptionsResponse>> getOptions(
            @Parameter(description = "DEVICE_PAGE 그룹의 페이지 코드") @PathVariable String pageCode) {
        return ResponseEntity.ok(ApiResponse.ok(queryService.getOptions(pageCode)));
    }
}
