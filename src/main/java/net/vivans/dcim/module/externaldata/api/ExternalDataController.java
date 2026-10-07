package net.vivans.dcim.module.externaldata.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.externaldata.api.dto.ExternalDataResponse;
import net.vivans.dcim.module.externaldata.api.dto.ExternalDataSaveRequest;
import net.vivans.dcim.module.externaldata.application.ExternalDataService;
import net.vivans.dcim.shared.api.ApiResponse;
import net.vivans.dcim.shared.api.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/internal/external-data")
@RequiredArgsConstructor
public class ExternalDataController {

    private final ExternalDataService service;

    @PostMapping
    public ResponseEntity<ApiResponse<ExternalDataResponse>> save(@Valid @RequestBody ExternalDataSaveRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                service.save(request.dataCategory(), request.periodType(), request.payload())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ExternalDataResponse>>> search(
            @RequestParam(required = false) String dataCategory,
            @RequestParam(required = false) String periodType,
            @RequestParam(required = false) Instant receivedFrom,
            @RequestParam(required = false) Instant receivedTo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok(service.search(
                dataCategory, periodType, receivedFrom, receivedTo, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExternalDataResponse>> get(@PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.get(id)));
    }
}
