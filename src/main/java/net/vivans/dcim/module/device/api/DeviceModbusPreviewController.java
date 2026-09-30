package net.vivans.dcim.module.device.api;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.device.application.DeviceModbusPreviewService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/devices/{deviceId}/modbus")
public class DeviceModbusPreviewController {
    private final DeviceModbusPreviewService previewService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/read-preview")
    public ApiResponse<DeviceModbusPreviewService.PreviewResult> read(@PathVariable Integer deviceId) {
        return ApiResponse.ok(previewService.read(deviceId));
    }
}
