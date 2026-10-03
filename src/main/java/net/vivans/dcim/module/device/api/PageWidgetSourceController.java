package net.vivans.dcim.module.device.api;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.device.application.DeviceMeasurementSourceCatalog;
import net.vivans.dcim.module.query.domain.LastPoint;
import net.vivans.dcim.module.query.domain.PointQuery;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/widgets/sources")
public class PageWidgetSourceController {
    private final DeviceMeasurementSourceCatalog sourceCatalog;
    private final PointQuery pointQuery;

    @GetMapping
    public ApiResponse<List<DeviceMeasurementSourceCatalog.Source>> sources() {
        return ApiResponse.ok(sourceCatalog.availableSources());
    }

    @GetMapping("/preview")
    public ApiResponse<LastPoint> preview(@RequestParam Integer deviceId,
                                          @RequestParam String pointName,
                                          @RequestParam(required = false) String protocol) {
        if (deviceId == null || deviceId <= 0 || pointName == null || pointName.isBlank()) {
            throw new IllegalArgumentException("deviceId and pointName are required");
        }
        boolean known = sourceCatalog.availableSources(Set.of(deviceId)).stream().anyMatch(source ->
                source.deviceId().equals(deviceId) && source.pointName().equals(pointName)
                        && !source.ambiguous()
                        && (protocol == null || source.protocol().equalsIgnoreCase(protocol)));
        if (!known) throw new IllegalArgumentException("선택할 수 없는 측정항목입니다");
        List<LastPoint> latest = pointQuery.findLast(List.of(deviceId), List.of(pointName),
                Duration.ofHours(24), protocol);
        return ApiResponse.ok(latest.isEmpty() ? null : latest.get(0));
    }
}
