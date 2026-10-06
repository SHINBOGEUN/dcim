package net.vivans.dcim.module.device.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.common.domain.repository.CommonCodeRepository;
import net.vivans.dcim.module.device.api.dto.DevicePageModelPointSettingsRequest;
import net.vivans.dcim.module.device.api.dto.DevicePageModelPointSettingsResponse;
import net.vivans.dcim.module.device.api.dto.DevicePageModelPointSettingsResponse.PointSetting;
import net.vivans.dcim.module.device.domain.model.DevicePageCodes;
import net.vivans.dcim.module.device.domain.model.DevicePageModelPointSetting;
import net.vivans.dcim.module.device.infrastructure.persistence.DevicePageModelPointSettingRepository;
import net.vivans.dcim.module.devicemodel.api.dto.DeviceModelMeasurementPointOptionsResponse;
import net.vivans.dcim.module.devicemodel.api.dto.DeviceModelMeasurementPointOptionsResponse.PointOption;
import net.vivans.dcim.module.devicemodel.application.DeviceModelMeasurementPointQueryService;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DevicePageModelPointSettingService {
    private static final String PROTOCOL_TYPE_GROUP_KEY = "PROTOCOL_TYPE";

    private final CommonCodeRepository commonCodeRepository;
    private final DeviceModelRepository deviceModelRepository;
    private final DevicePageModelPointSettingRepository settingRepository;
    private final DeviceModelMeasurementPointQueryService pointQueryService;

    public DevicePageModelPointSettingsResponse getSettings(String pageCode, Integer modelId) {
        CommonCode page = findPage(pageCode);
        DeviceModel model = findModel(modelId);
        DeviceModelMeasurementPointOptionsResponse catalog = pointQueryService.getMeasurementPoints(modelId);

        Map<PointIdentity, DevicePageModelPointSetting> savedByPoint = new HashMap<>();
        for (DevicePageModelPointSetting setting : settingRepository
                .findAllByPageCode_IdAndDeviceModel_IdOrderBySortOrderAscIdAsc(page.getId(), modelId)) {
            savedByPoint.put(new PointIdentity(setting.getProtocolType().getId(), setting.getPointId()), setting);
        }

        List<PointSetting> points = catalog.points().stream().map(option -> {
            DevicePageModelPointSetting setting = savedByPoint.get(identity(option));
            return toResponse(option, setting != null && setting.isVisible(),
                    setting == null ? 0 : setting.getSortOrder());
        }).sorted(java.util.Comparator.comparingInt(PointSetting::sortOrder)
                .thenComparing(PointSetting::protocol)
                .thenComparing(PointSetting::pointName)).toList();
        return new DevicePageModelPointSettingsResponse(page.getCode(), model.getId(), model.getName(), points);
    }

    @Transactional
    public DevicePageModelPointSettingsResponse replaceSettings(
            String pageCode, Integer modelId, DevicePageModelPointSettingsRequest request) {
        CommonCode page = findPage(pageCode);
        DeviceModel model = findModel(modelId);
        DeviceModelMeasurementPointOptionsResponse catalog = pointQueryService.getMeasurementPoints(modelId);

        Map<PointIdentity, PointOption> catalogByPoint = new HashMap<>();
        for (PointOption point : catalog.points()) {
            catalogByPoint.put(identity(point), point);
        }

        Map<PointIdentity, DevicePageModelPointSettingsRequest.PointSetting> requestedByPoint = new HashMap<>();
        for (DevicePageModelPointSettingsRequest.PointSetting requested : request.points()) {
            PointIdentity identity = new PointIdentity(requested.protocolTypeId(), requested.pointId());
            if (!catalogByPoint.containsKey(identity)) {
                throw new IllegalArgumentException("Point does not belong to model/protocol: "
                        + requested.protocolTypeId() + "/" + requested.pointId());
            }
            if (requestedByPoint.putIfAbsent(identity, requested) != null) {
                throw new IllegalArgumentException("Duplicate point setting: "
                        + requested.protocolTypeId() + "/" + requested.pointId());
            }
        }

        List<DevicePageModelPointSetting> existingSettings = settingRepository
                .findAllByPageCode_IdAndDeviceModel_IdOrderBySortOrderAscIdAsc(page.getId(), modelId);
        Map<PointIdentity, DevicePageModelPointSetting> existingByPoint = new HashMap<>();
        for (DevicePageModelPointSetting setting : existingSettings) {
            existingByPoint.put(
                    new PointIdentity(setting.getProtocolType().getId(), setting.getPointId()), setting);
        }

        Map<Integer, CommonCode> protocolTypes = new HashMap<>();
        Set<PointIdentity> catalogIdentities = new HashSet<>();
        List<DevicePageModelPointSetting> replacements = new java.util.ArrayList<>(catalog.points().size());
        int catalogOrder = 0;
        for (PointOption point : catalog.points()) {
            PointIdentity identity = identity(point);
            if (!catalogIdentities.add(identity)) {
                throw new IllegalStateException("Duplicate point in model catalog: "
                        + point.protocolTypeId() + "/" + point.pointId());
            }
            DevicePageModelPointSettingsRequest.PointSetting requested = requestedByPoint.get(identity);
            boolean visible = requested != null && requested.visible();
            int sortOrder = requested == null || requested.sortOrder() == null
                    ? catalogOrder : requested.sortOrder();
            DevicePageModelPointSetting existing = existingByPoint.get(identity);
            if (existing != null) {
                existing.update(visible, sortOrder);
                replacements.add(existing);
            } else {
                CommonCode protocolType = protocolTypes.computeIfAbsent(point.protocolTypeId(), id ->
                        commonCodeRepository.findById(id)
                                .filter(code -> PROTOCOL_TYPE_GROUP_KEY.equals(code.getCodeGroup().getGroupKey()))
                                .orElseThrow(() -> new EntityNotFoundException(
                                        "PROTOCOL_TYPE code not found: " + id)));
                replacements.add(DevicePageModelPointSetting.create(
                        page, model, protocolType, point.pointId(), visible, sortOrder));
            }
            catalogOrder++;
        }

        List<DevicePageModelPointSetting> staleSettings = existingSettings.stream()
                .filter(setting -> !catalogIdentities.contains(
                        new PointIdentity(setting.getProtocolType().getId(), setting.getPointId())))
                .toList();
        if (!staleSettings.isEmpty()) {
            settingRepository.deleteAll(staleSettings);
        }
        settingRepository.saveAll(replacements);
        return getSettings(pageCode, modelId);
    }

    private CommonCode findPage(String pageCode) {
        if (pageCode == null || pageCode.isBlank()) {
            throw new IllegalArgumentException("pageCode is required");
        }
        return commonCodeRepository.findByCodeGroupGroupKeyAndCode(
                        DevicePageCodes.DEVICE_PAGE_GROUP_KEY, pageCode.trim())
                .orElseThrow(() -> new EntityNotFoundException(
                        "DEVICE_PAGE code not found: " + pageCode.trim()));
    }

    private DeviceModel findModel(Integer modelId) {
        return deviceModelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("DeviceModel not found: " + modelId));
    }

    private static PointIdentity identity(PointOption point) {
        return new PointIdentity(point.protocolTypeId(), point.pointId());
    }

    private static PointSetting toResponse(PointOption point, boolean visible, int sortOrder) {
        return new PointSetting(point.protocolTypeId(), point.protocol(), point.pointId(), point.pointName(),
                point.unit(), point.valueType(), point.dataPointTypeId(), point.dataPointType(),
                point.categoryCodeId(), point.categoryCode(), point.categoryName(),
                point.requiresInstance(), point.collectionEnabled(),
                visible, sortOrder);
    }

    private record PointIdentity(Integer protocolTypeId, Integer pointId) {
    }
}
