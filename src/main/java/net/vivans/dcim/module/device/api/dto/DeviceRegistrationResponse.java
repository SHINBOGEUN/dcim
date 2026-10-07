package net.vivans.dcim.module.device.api.dto;

public record DeviceRegistrationResponse(
        DeviceResponse device,
        Integer endpointId,
        Integer taskId,
        Integer groupId
) {
}
