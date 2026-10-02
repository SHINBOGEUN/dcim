package net.vivans.dcim.module.calculated.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.calculated.api.dto.CalculatedMetricRequest;
import net.vivans.dcim.module.calculated.api.dto.CalculatedMetricResponse;
import net.vivans.dcim.module.calculated.api.dto.CalculatedMetricStatusResponse;
import net.vivans.dcim.module.calculated.application.CalculatedMetricStatusService;
import net.vivans.dcim.module.calculated.application.CalculatedMetricService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/calculated-metrics")
public class CalculatedMetricController {
    private final CalculatedMetricService service;
    private final CalculatedMetricStatusService statusService;

    @GetMapping
    public ApiResponse<List<CalculatedMetricResponse>> list() { return ApiResponse.ok(service.listCalculated()); }

    @GetMapping("/{id}/status")
    public ApiResponse<CalculatedMetricStatusResponse> status(@PathVariable Integer id) {
        return ApiResponse.ok(statusService.getStatus(id));
    }

    @GetMapping("/sources")
    public ApiResponse<List<CalculatedMetricService.SourceOption>> sources() {
        return ApiResponse.ok(service.availableCalculatedSources());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<CalculatedMetricResponse> create(@Valid @RequestBody CalculatedMetricRequest request) {
        return ApiResponse.ok(service.createCalculated(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<CalculatedMetricResponse> update(@PathVariable Integer id, @Valid @RequestBody CalculatedMetricRequest request) {
        return ApiResponse.ok(service.updateCalculated(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/collection-enabled")
    public ApiResponse<CalculatedMetricResponse> toggle(@PathVariable Integer id, @RequestParam boolean enabled) {
        return ApiResponse.ok(service.setCollectionEnabled(id, enabled));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Integer> delete(@PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.ok(id);
    }
}
