package net.vivans.dcim.module.device.api.dto;

import net.vivans.dcim.module.device.domain.model.DeviceProtocolEndpoint;

public record DeviceProtocolEndpointResponse(
        Integer id,
        Integer deviceId,
        Integer protocolTypeId,
        String protocolCode,
        String protocolName,
        String host,
        int port,
        boolean enabled
) {

    public static DeviceProtocolEndpointResponse from(DeviceProtocolEndpoint endpoint) {
        return new DeviceProtocolEndpointResponse(
                endpoint.getId(),
                endpoint.getDevice().getId(),
                endpoint.getProtocolType().getId(),
                endpoint.getProtocolType().getCode(),
                endpoint.getProtocolType().getName(),
                endpoint.getHost(),
                endpoint.getPort(),
                endpoint.isEnabled()
        );
    }
}
