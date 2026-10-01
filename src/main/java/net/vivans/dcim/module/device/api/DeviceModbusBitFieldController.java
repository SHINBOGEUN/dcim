package net.vivans.dcim.module.device.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.device.api.dto.DeviceModbusBitFieldResponse;
import net.vivans.dcim.module.device.api.dto.DeviceModbusBitFieldsReplaceRequest;
import net.vivans.dcim.module.device.application.DeviceModbusBitFieldService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/devices/{deviceId}/endpoints/{endpointId}/modbus/readings/{readingId}/bit-fields")
public class DeviceModbusBitFieldController {
    private final DeviceModbusBitFieldService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceModbusBitFieldResponse>>> get(
            @PathVariable Integer deviceId, @PathVariable Integer endpointId, @PathVariable Integer readingId) {
        return ResponseEntity.ok(ApiResponse.ok(service.get(deviceId, endpointId, readingId)));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DeviceModbusBitFieldResponse>>> replace(
            @PathVariable Integer deviceId, @PathVariable Integer endpointId, @PathVariable Integer readingId,
            @Valid @RequestBody DeviceModbusBitFieldsReplaceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.replace(deviceId, endpointId, readingId, request.fields())));
    }
}
