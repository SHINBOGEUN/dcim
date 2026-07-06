package net.vivans.dcim.module.devicemodel.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.common.domain.repository.CommonCodeRepository;
import net.vivans.dcim.module.devicemodel.api.dto.DeviceModelCreateRequest;
import net.vivans.dcim.module.devicemodel.api.dto.DeviceModelProtocolRequest;
import net.vivans.dcim.module.devicemodel.api.dto.DeviceModelResponse;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelProtocol;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceModelQueryService {

    private static final String PROTOCOL_TYPE_GROUP_KEY = "PROTOCOL_TYPE";

    private final DeviceModelRepository deviceModelRepository;
    private final CommonCodeRepository commonCodeRepository;

    @Transactional
    public DeviceModelResponse createDeviceModel(DeviceModelCreateRequest request) {
        validateUniqueNameAndManufacturer(request.name(), request.manufacturer(), null);

        DeviceModel deviceModel = DeviceModel.create(
                request.name(),
                request.manufacturer(),
                request.description()
        );
        replaceProtocolsFromRequest(deviceModel, request.protocols());

        return DeviceModelResponse.from(deviceModelRepository.save(deviceModel));
    }

    @Transactional
    public DeviceModelResponse updateDeviceModel(Integer id, DeviceModelCreateRequest request) {
        DeviceModel deviceModel = findDeviceModel(id);
        validateUniqueNameAndManufacturer(request.name(), request.manufacturer(), id);

        deviceModel.update(request.name(), request.manufacturer(), request.description());
        replaceProtocolsFromRequest(deviceModel, request.protocols());

        return DeviceModelResponse.from(deviceModelRepository.save(deviceModel));
    }

    public List<DeviceModelResponse> getDeviceModels(String name, String manufacturer) {
        List<DeviceModel> deviceModels = deviceModelRepository.findAll(name, manufacturer);
        List<DeviceModelResponse> responses = new ArrayList<>();
        for (DeviceModel deviceModel : deviceModels) {
            responses.add(DeviceModelResponse.from(deviceModel));
        }
        return responses;
    }

    public DeviceModelResponse getDeviceModel(Integer id) {
        return DeviceModelResponse.from(findDeviceModel(id));
    }

    @Transactional
    public void deleteDeviceModel(Integer id) {
        DeviceModel deviceModel = findDeviceModel(id);
        deviceModelRepository.delete(deviceModel);
    }

    private DeviceModel findDeviceModel(Integer id) {
        return deviceModelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DeviceModel not found: " + id));
    }

    private void validateUniqueNameAndManufacturer(String name, String manufacturer, Integer excludeId) {
        boolean exists = excludeId == null
                ? deviceModelRepository.existsByNameAndManufacturer(name, manufacturer)
                : deviceModelRepository.existsByNameAndManufacturerAndIdNot(name, manufacturer, excludeId);
        if (exists) {
            throw new IllegalArgumentException("device model already exists");
        }
    }

    /**
     * API 요청의 protocols[]를 검증한 뒤, DeviceModel에 연결된 프로토콜 목록을 통째로 교체합니다.
     * 등록·수정 모두 동일하게 사용합니다 (수정 시 기존 device_model_protocol 행은 전부 삭제 후 재등록).
     */
    private void replaceProtocolsFromRequest(DeviceModel deviceModel, List<DeviceModelProtocolRequest> protocolRequests) {
        validateProtocolRequests(protocolRequests);
        List<Boolean> defaultFlags = resolveDefaultFlags(protocolRequests);

        List<DeviceModelProtocol> protocols = new ArrayList<>();
        for (int i = 0; i < protocolRequests.size(); i++) {
            DeviceModelProtocolRequest request = protocolRequests.get(i);
            CommonCode protocolType = findProtocolType(request.protocolTypeId());
            int sortOrder = request.sortOrder() != null ? request.sortOrder() : i + 1;
            protocols.add(DeviceModelProtocol.of(
                    deviceModel,
                    protocolType,
                    defaultFlags.get(i),
                    sortOrder
            ));
        }
        deviceModel.replaceProtocols(protocols);
    }

    private void validateProtocolRequests(List<DeviceModelProtocolRequest> protocolRequests) {
        if (protocolRequests == null || protocolRequests.isEmpty()) {
            throw new IllegalArgumentException("at least one protocol required");
        }

        Set<Integer> protocolTypeIds = new HashSet<>();
        for (DeviceModelProtocolRequest request : protocolRequests) {
            if (!protocolTypeIds.add(request.protocolTypeId())) {
                throw new IllegalArgumentException("duplicate protocol type in request");
            }
        }
    }

    private List<Boolean> resolveDefaultFlags(List<DeviceModelProtocolRequest> protocolRequests) {
        if (protocolRequests.size() == 1) {
            return List.of(true);
        }

        int defaultCount = 0;
        for (DeviceModelProtocolRequest request : protocolRequests) {
            if (Boolean.TRUE.equals(request.isDefault())) {
                defaultCount++;
            }
        }
        if (defaultCount != 1) {
            throw new IllegalArgumentException("exactly one default protocol required");
        }

        List<Boolean> defaultFlags = new ArrayList<>();
        for (DeviceModelProtocolRequest request : protocolRequests) {
            defaultFlags.add(Boolean.TRUE.equals(request.isDefault()));
        }
        return defaultFlags;
    }

    private CommonCode findProtocolType(Integer protocolTypeId) {
        CommonCode protocolType = commonCodeRepository.findById(protocolTypeId)
                .orElseThrow(() -> new EntityNotFoundException("CommonCode not found: " + protocolTypeId));

        if (!PROTOCOL_TYPE_GROUP_KEY.equals(protocolType.getCodeGroup().getGroupKey())) {
            throw new IllegalArgumentException("protocolType must belong to PROTOCOL_TYPE group");
        }
        return protocolType;
    }
}
