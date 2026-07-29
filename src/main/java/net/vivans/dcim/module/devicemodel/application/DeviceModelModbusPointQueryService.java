package net.vivans.dcim.module.devicemodel.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.devicemodel.api.dto.DeviceModelModbusPointCreateRequest;
import net.vivans.dcim.module.devicemodel.api.dto.DeviceModelModbusPointResponse;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelModbusPoint;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelProtocol;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelModbusPointRepository;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceModelModbusPointQueryService {

    private final DeviceModelRepository deviceModelRepository;
    private final DeviceModelModbusPointRepository deviceModelModbusPointRepository;

    public List<DeviceModelModbusPointResponse> getDeviceModelModbusPoints(Integer modelId, Integer protocolId) {
        findModbusProtocol(modelId, protocolId);

        List<DeviceModelModbusPoint> points =
                deviceModelModbusPointRepository.findAllByModelProtocolIdOrderByIdAsc(protocolId);

        List<DeviceModelModbusPointResponse> responses = new ArrayList<>();
        for (DeviceModelModbusPoint point : points) {
            responses.add(DeviceModelModbusPointResponse.from(point));
        }
        return responses;
    }

    public DeviceModelModbusPointResponse getDeviceModelModbusPoint(
            Integer modelId,
            Integer protocolId,
            Integer pointId
    ) {
        findModbusProtocol(modelId, protocolId);
        return DeviceModelModbusPointResponse.from(findModbusPoint(pointId, protocolId));
    }

    @Transactional
    public DeviceModelModbusPointResponse createDeviceModelModbusPoint(
            Integer modelId,
            Integer protocolId,
            DeviceModelModbusPointCreateRequest request
    ) {
        DeviceModelProtocol protocol = findModbusProtocol(modelId, protocolId);

        if (deviceModelModbusPointRepository.existsByModelProtocolIdAndName(protocolId, request.name())) {
            throw new IllegalArgumentException("point name already exists for this protocol");
        }

        boolean requiresInstance = Boolean.TRUE.equals(request.requiresInstance());
        boolean enabled = request.enabled() == null || request.enabled();

        DeviceModelModbusPoint point = DeviceModelModbusPoint.create(
                protocol,
                request.name(),
                request.registerType(),
                request.dataType(),
                request.byteOrder(),
                request.address(),
                requiresInstance,
                request.scale(),
                request.unit(),
                enabled
        );

        return DeviceModelModbusPointResponse.from(deviceModelModbusPointRepository.save(point));
    }

    @Transactional
    public DeviceModelModbusPointResponse updateDeviceModelModbusPoint(
            Integer modelId,
            Integer protocolId,
            Integer pointId,
            DeviceModelModbusPointCreateRequest request
    ) {
        findModbusProtocol(modelId, protocolId);
        DeviceModelModbusPoint point = findModbusPoint(pointId, protocolId);

        if (deviceModelModbusPointRepository.existsByModelProtocolIdAndNameAndIdNot(
                protocolId, request.name(), pointId)) {
            throw new IllegalArgumentException("point name already exists for this protocol");
        }

        boolean requiresInstance = Boolean.TRUE.equals(request.requiresInstance());
        boolean enabled = request.enabled() == null || request.enabled();

        point.update(
                request.name(),
                request.registerType(),
                request.dataType(),
                request.byteOrder(),
                request.address(),
                requiresInstance,
                request.scale(),
                request.unit(),
                enabled
        );

        return DeviceModelModbusPointResponse.from(deviceModelModbusPointRepository.save(point));
    }

    @Transactional
    public Integer deleteDeviceModelModbusPoint(Integer modelId, Integer protocolId, Integer pointId) {
        findModbusProtocol(modelId, protocolId);
        DeviceModelModbusPoint point = findModbusPoint(pointId, protocolId);
        deviceModelModbusPointRepository.delete(point);
        return pointId;
    }

    private DeviceModelProtocol findModbusProtocol(Integer modelId, Integer protocolId) {
        DeviceModel deviceModel = deviceModelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("DeviceModel not found: " + modelId));

        for (DeviceModelProtocol protocol : deviceModel.getProtocols()) {
            if (protocolId.equals(protocol.getId())) {
                return protocol;
            }
        }
        throw new EntityNotFoundException("DeviceModelProtocol not found: " + protocolId);
    }

    private DeviceModelModbusPoint findModbusPoint(Integer pointId, Integer protocolId) {
        return deviceModelModbusPointRepository.findByIdAndModelProtocolId(pointId, protocolId)
                .orElseThrow(() -> new EntityNotFoundException("DeviceModelModbusPoint not found: " + pointId));
    }
}