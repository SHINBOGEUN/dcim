package net.vivans.dcim.module.pue.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.pue.api.dto.CalculatedMetricRequest;
import net.vivans.dcim.module.pue.api.dto.PueDefinitionResponse;
import net.vivans.dcim.module.pue.api.dto.CalculatedMetricStatusResponse;
import net.vivans.dcim.module.pue.application.CalculatedMetricStatusService;
import net.vivans.dcim.module.pue.application.PueDefinitionService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/calculated-metrics")
public class CalculatedMetricController {
    private final PueDefinitionService service;
    private final CalculatedMetricStatusService statusService;

    @GetMapping
    public ApiResponse<List<PueDefinitionResponse>> list() { return ApiResponse.ok(service.listCalculated()); }

    @GetMapping("/{id}/status")
    public ApiResponse<CalculatedMetricStatusResponse> status(@PathVariable Integer id) {
        return ApiResponse.ok(statusService.getStatus(id));
    }

    @GetMapping("/sources")
    public ApiResponse<List<PueDefinitionService.SourceOption>> sources() {
        return ApiResponse.ok(service.availableCalculatedSources());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<PueDefinitionResponse> create(@Valid @RequestBody CalculatedMetricRequest request) {
        return ApiResponse.ok(service.createCalculated(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<PueDefinitionResponse> update(@PathVariable Integer id, @Valid @RequestBody CalculatedMetricRequest request) {
        return ApiResponse.ok(service.updateCalculated(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/collection-enabled")
    public ApiResponse<PueDefinitionResponse> toggle(@PathVariable Integer id, @RequestParam boolean enabled) {
        return ApiResponse.ok(service.setCollectionEnabled(id, enabled));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Integer> delete(@PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.ok(id);
    }
}
