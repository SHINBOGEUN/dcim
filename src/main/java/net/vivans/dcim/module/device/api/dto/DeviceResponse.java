package net.vivans.dcim.module.device.api.dto;

import net.vivans.dcim.module.device.domain.model.Device;

public record DeviceResponse(
        Integer id,
        Integer modelId,
        String modelName,
        String manufacturer,
        String locationNodeName,
        String name,
        String description,
        boolean enabled
) {

    public static DeviceResponse from(Device device) {
        return new DeviceResponse(
                device.getId(),
                device.getDeviceModel().getId(),
                device.getDeviceModel().getName(),
                device.getDeviceModel().getManufacturer(),
                device.getLocationNode().getName(),
                device.getName(),
                device.getDescription(),
                device.isEnabled()
        );
    }
}
