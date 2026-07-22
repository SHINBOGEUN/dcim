package net.vivans.dcim.module.device.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.device.api.dto.DeviceCreateRequest;
import net.vivans.dcim.module.device.api.dto.DeviceResponse;
import net.vivans.dcim.module.device.domain.model.Device;
import net.vivans.dcim.module.device.domain.repository.DeviceRepository;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelRepository;
import net.vivans.dcim.module.location.domain.model.LocationNode;
import net.vivans.dcim.module.location.domain.repository.LocationNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceQueryService {

    private final DeviceRepository deviceRepository;
    private final DeviceModelRepository deviceModelRepository;
    private final LocationNodeRepository locationNodeRepository;

    @Transactional
    public DeviceResponse createDevice(DeviceCreateRequest request) {
        DeviceModel deviceModel = findDeviceModel(request.modelId());
        LocationNode locationNode = findLocationNode(request.locationNodeCode());
        validateUniqueNameAtLocation(locationNode, request.name());

        boolean enabled = request.enabled() == null || request.enabled();
        Device device = Device.create(
                deviceModel,
                locationNode,
                request.name(),
                request.description(),
                enabled
        );
        return DeviceResponse.from(deviceRepository.save(device));
    }

    private DeviceModel findDeviceModel(Integer modelId) {
        return deviceModelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("DeviceModel not found: " + modelId));
    }

    private LocationNode findLocationNode(String locationNodeCode) {
        return locationNodeRepository.findByCode(locationNodeCode)
                .orElseThrow(() -> new EntityNotFoundException("LocationNode not found: " + locationNodeCode));
    }

    private void validateUniqueNameAtLocation(LocationNode locationNode, String name) {
        if (deviceRepository.existsByLocationNodeAndName(locationNode, name)) {
            throw new IllegalArgumentException("device name already exists at this location");
        }
    }
}
