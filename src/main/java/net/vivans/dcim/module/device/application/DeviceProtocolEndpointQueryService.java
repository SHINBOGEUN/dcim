package net.vivans.dcim.module.device.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.common.domain.repository.CommonCodeRepository;
import net.vivans.dcim.module.device.api.dto.DeviceProtocolEndpointCreateRequest;
import net.vivans.dcim.module.device.api.dto.DeviceProtocolEndpointResponse;
import net.vivans.dcim.module.device.domain.model.Device;
import net.vivans.dcim.module.device.domain.model.DeviceProtocolEndpoint;
import net.vivans.dcim.module.device.domain.repository.DeviceProtocolEndpointRepository;
import net.vivans.dcim.module.device.domain.repository.DeviceRepository;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelProtocol;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelRepository;
import net.vivans.dcim.shared.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceProtocolEndpointQueryService {

    private static final String PROTOCOL_TYPE_GROUP_KEY = "PROTOCOL_TYPE";
    private static final String ENDPOINT_ALREADY_EXISTS_MESSAGE = "endpoint already exists for this protocol";
    private static final String PROTOCOL_NOT_SUPPORTED_MESSAGE = "protocol not supported by device model";

    private final DeviceRepository deviceRepository;
    private final DeviceModelRepository deviceModelRepository;
    private final DeviceProtocolEndpointRepository deviceProtocolEndpointRepository;
    private final CommonCodeRepository commonCodeRepository;

    @Transactional
    public DeviceProtocolEndpointResponse createEndpoint(
            Integer deviceId,
            DeviceProtocolEndpointCreateRequest request
    ) {
        Device device = findDevice(deviceId);
        CommonCode protocolType = findProtocolType(request.protocolTypeId());
        validateProtocolSupportedByModel(device, protocolType);

        if (deviceProtocolEndpointRepository.existsByDeviceIdAndProtocolTypeId(
                deviceId, protocolType.getId())) {
            throw new ConflictException(ENDPOINT_ALREADY_EXISTS_MESSAGE);
        }

        boolean enabled = request.enabled() == null || request.enabled();
        DeviceProtocolEndpoint endpoint = DeviceProtocolEndpoint.create(
                device,
                protocolType,
                request.host(),
                request.port(),
                enabled
        );
        return DeviceProtocolEndpointResponse.from(deviceProtocolEndpointRepository.save(endpoint));
    }

    @Transactional
    public DeviceProtocolEndpointResponse updateEndpoint(
            Integer deviceId,
            Integer endpointId,
            DeviceProtocolEndpointCreateRequest request
    ) {
        Device device = findDevice(deviceId);
        DeviceProtocolEndpoint endpoint = findEndpoint(endpointId, deviceId);
        CommonCode protocolType = findProtocolType(request.protocolTypeId());
        validateProtocolSupportedByModel(device, protocolType);

        if (deviceProtocolEndpointRepository.existsByDeviceIdAndProtocolTypeIdAndIdNot(
                deviceId, protocolType.getId(), endpointId)) {
            throw new ConflictException(ENDPOINT_ALREADY_EXISTS_MESSAGE);
        }

        boolean enabled = request.enabled() == null || request.enabled();
        endpoint.update(protocolType, request.host(), request.port(), enabled);
        return DeviceProtocolEndpointResponse.from(deviceProtocolEndpointRepository.save(endpoint));
    }

    private Device findDevice(Integer deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Device not found: " + deviceId));
    }

    private DeviceProtocolEndpoint findEndpoint(Integer endpointId, Integer deviceId) {
        return deviceProtocolEndpointRepository.findByIdAndDeviceId(endpointId, deviceId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "DeviceProtocolEndpoint not found: " + endpointId));
    }

    private CommonCode findProtocolType(Integer protocolTypeId) {
        CommonCode protocolType = commonCodeRepository.findById(protocolTypeId)
                .orElseThrow(() -> new EntityNotFoundException("CommonCode not found: " + protocolTypeId));
        if (!PROTOCOL_TYPE_GROUP_KEY.equals(protocolType.getCodeGroup().getGroupKey())) {
            throw new IllegalArgumentException("protocolType must belong to PROTOCOL_TYPE group");
        }
        return protocolType;
    }

    private void validateProtocolSupportedByModel(Device device, CommonCode protocolType) {
        DeviceModel deviceModel = deviceModelRepository.findById(device.getDeviceModel().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "DeviceModel not found: " + device.getDeviceModel().getId()));

        for (DeviceModelProtocol protocol : deviceModel.getProtocols()) {
            if (protocolType.getId().equals(protocol.getProtocolType().getId())) {
                return;
            }
        }
        throw new IllegalArgumentException(PROTOCOL_NOT_SUPPORTED_MESSAGE);
    }
}
