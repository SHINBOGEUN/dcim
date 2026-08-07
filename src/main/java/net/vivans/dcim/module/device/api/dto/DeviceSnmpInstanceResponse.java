package net.vivans.dcim.module.device.api.dto;

import net.vivans.dcim.module.device.domain.model.DeviceSnmpInstance;

public record DeviceSnmpInstanceResponse(
        Integer endpointId,
        Integer deviceId,
        int instanceId
) {

    public static DeviceSnmpInstanceResponse from(DeviceSnmpInstance snmpInstance) {
        return new DeviceSnmpInstanceResponse(
                snmpInstance.getEndpointId(),
                snmpInstance.getEndpoint().getDevice().getId(),
                snmpInstance.getInstanceId()
        );
    }
}
