package net.vivans.dcim.module.live.application;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.device.api.dto.DeviceCapabilityPointResponse;
import net.vivans.dcim.module.device.api.dto.DeviceCapabilityResponse;
import net.vivans.dcim.module.device.application.DeviceCapabilityQueryService;
import net.vivans.dcim.module.device.application.DeviceMeasurementSourceCatalog;
import net.vivans.dcim.module.device.domain.repository.DeviceRepository;
import net.vivans.dcim.module.collectortask.application.CollectionGroupSpecService;
import net.vivans.dcim.module.collectortask.application.CollectionGroupModbusSpec;
import net.vivans.dcim.module.live.api.dto.LiveDeviceResponse;
import net.vivans.dcim.module.live.api.dto.LivePointResponse;
import net.vivans.dcim.module.live.api.dto.LiveSelectionItemRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LiveTelemetryQueryService {

    private final DeviceCapabilityQueryService deviceCapabilityQueryService;
    private final DeviceMeasurementSourceCatalog measurementSourceCatalog;
    private final DeviceRepository deviceRepository;
    private final CollectionGroupSpecService collectionGroupSpecService;

    public List<LiveDeviceResponse> getSelectableDevices() {
        List<LiveDeviceResponse> devices = new ArrayList<>();
        for (DeviceCapabilityResponse capability : deviceCapabilityQueryService.getCapabilities(null, null, null)) {
            LiveDeviceResponse device = toSelectableDevice(capability);
            if (device != null) {
                devices.add(device);
            }
        }
        Map<Integer, List<LivePointResponse>> pointsByDevice = new LinkedHashMap<>();
        Map<Integer, LiveDeviceResponse> descriptions = new LinkedHashMap<>();
        for (LiveDeviceResponse device : devices) {
            descriptions.put(device.deviceId(), device);
            pointsByDevice.put(device.deviceId(), new ArrayList<>(device.points()));
        }
        Map<Integer, CollectionGroupSpecService.ModbusPreviewPlan> plans = new HashMap<>();
        Map<Integer, String> sourceNames = new HashMap<>();
        for (DeviceMeasurementSourceCatalog.Source source : measurementSourceCatalog.availableSources()) {
            if (!"modbus".equals(source.protocol())) continue;
            CollectionGroupSpecService.ModbusPreviewPlan plan = plans.computeIfAbsent(source.sourceDeviceId(), id ->
                    deviceRepository.findById(id).map(collectionGroupSpecService::previewModbus)
                            .orElse(new CollectionGroupSpecService.ModbusPreviewPlan(List.of(), List.of())));
            if (!isCollectible(plan, source.deviceId(), source.pointName())) continue;
            LiveDeviceResponse description = descriptions.computeIfAbsent(source.deviceId(), id ->
                    deviceRepository.findById(id).map(device -> new LiveDeviceResponse(
                            id, device.getName(), device.getLocationNode().getName(),
                            device.getDeviceModel().getId(), device.getDeviceModel().getName(), List.of()))
                            .orElse(null));
            if (description == null) continue;
            String sourceName = sourceNames.computeIfAbsent(source.sourceDeviceId(), id ->
                    deviceRepository.findById(id).map(device -> device.getName()).orElse("#" + id));
            List<LivePointResponse> points = pointsByDevice.computeIfAbsent(source.deviceId(), id -> new ArrayList<>());
            boolean duplicate = points.stream().anyMatch(point -> "modbus".equals(point.protocol())
                    && source.sourceDeviceId().equals(point.sourceDeviceId())
                    && source.pointName().equals(point.name()));
            if (!duplicate) points.add(new LivePointResponse(source.pointName(), source.unit(),
                    "modbus", source.sourceDeviceId(), sourceName));
        }
        return descriptions.values().stream()
                .filter(device -> device != null && !pointsByDevice.getOrDefault(device.deviceId(), List.of()).isEmpty())
                .map(device -> new LiveDeviceResponse(device.deviceId(), device.deviceName(),
                        device.locationNodeName(), device.modelId(), device.modelName(),
                        List.copyOf(pointsByDevice.get(device.deviceId()))))
                .toList();
    }

    private static boolean isCollectible(CollectionGroupSpecService.ModbusPreviewPlan plan,
                                         Integer storageDeviceId, String pointName) {
        for (CollectionGroupModbusSpec.ModbusTarget target : plan.targets()) {
            if (!storageDeviceId.equals(target.deviceId())) continue;
            for (CollectionGroupModbusSpec.ModbusPoint point : target.points()) {
                if (pointName.equals(point.name()) || point.bitFields() != null && point.bitFields().stream()
                        .anyMatch(bit -> pointName.equals(bit.name()))) return true;
            }
        }
        return false;
    }

    public List<LiveSelectionItemRequest> normalizeAndValidate(List<LiveSelectionItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        Map<SelectionKey, Set<String>> allowedPointsBySelection = allowedPointsBySelection();
        Set<SelectionKey> seenSelections = new LinkedHashSet<>();
        List<LiveSelectionItemRequest> normalized = new ArrayList<>();
        for (LiveSelectionItemRequest item : items) {
            if (item == null || item.deviceId() == null) {
                throw new IllegalArgumentException("deviceId is required");
            }
            String protocol = item.protocol() == null ? "snmp" : item.protocol().toLowerCase(Locale.ROOT);
            if (!"snmp".equals(protocol) && !"modbus".equals(protocol)) {
                throw new IllegalArgumentException("unsupported live protocol: " + protocol);
            }
            Integer sourceDeviceId = item.sourceDeviceId() == null && "snmp".equals(protocol)
                    ? item.deviceId() : item.sourceDeviceId();
            if (sourceDeviceId == null) throw new IllegalArgumentException("sourceDeviceId is required for Modbus");
            SelectionKey key = new SelectionKey(item.deviceId(), protocol, sourceDeviceId);
            if (!seenSelections.add(key)) {
                throw new IllegalArgumentException("duplicate live selection: " + key);
            }
            Set<String> allowedPoints = allowedPointsBySelection.get(key);
            if (allowedPoints == null) {
                throw new IllegalArgumentException("device is not selectable for live "
                        + protocol.toUpperCase(Locale.ROOT) + ": " + item.deviceId());
            }
            List<String> pointNames = normalizePointNames(item.pointNames(), item.deviceId(), allowedPoints);
            normalized.add(new LiveSelectionItemRequest(item.deviceId(), pointNames, protocol, sourceDeviceId));
        }
        return List.copyOf(normalized);
    }

    private Map<SelectionKey, Set<String>> allowedPointsBySelection() {
        Map<SelectionKey, Set<String>> allowed = new LinkedHashMap<>();
        for (LiveDeviceResponse device : getSelectableDevices()) {
            for (LivePointResponse point : device.points()) {
                allowed.computeIfAbsent(new SelectionKey(device.deviceId(), point.protocol(),
                        point.sourceDeviceId()), ignored -> new LinkedHashSet<>()).add(point.name());
            }
        }
        return allowed;
    }

    private static List<String> normalizePointNames(
            List<String> rawNames,
            Integer deviceId,
            Set<String> allowedPoints
    ) {
        if (rawNames == null || rawNames.isEmpty()) {
            throw new IllegalArgumentException("pointNames must not be empty: deviceId=" + deviceId);
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String rawName : rawNames) {
            if (rawName == null || rawName.isBlank()) {
                throw new IllegalArgumentException("pointName must not be blank: deviceId=" + deviceId);
            }
            String pointName = rawName.trim();
            if (!allowedPoints.contains(pointName)) {
                throw new IllegalArgumentException(
                        "unknown pointName '" + pointName + "' for device " + deviceId);
            }
            unique.add(pointName);
        }
        return List.copyOf(unique);
    }

    private static LiveDeviceResponse toSelectableDevice(DeviceCapabilityResponse capability) {
        if (capability.endpoint() == null) {
            return null;
        }
        List<LivePointResponse> points = new ArrayList<>();
        for (DeviceCapabilityPointResponse point : capability.points()) {
            if (point.resolvedOid() == null || point.resolvedOid().isBlank()) {
                continue;
            }
            points.add(new LivePointResponse(point.name(), point.unit(), "snmp",
                    capability.deviceId(), capability.deviceName()));
        }
        if (points.isEmpty()) {
            return null;
        }
        return new LiveDeviceResponse(
                capability.deviceId(),
                capability.deviceName(),
                capability.locationNodeName(),
                capability.modelId(),
                capability.modelName(),
                List.copyOf(points)
        );
    }

    private record SelectionKey(Integer deviceId, String protocol, Integer sourceDeviceId) {
    }
}
