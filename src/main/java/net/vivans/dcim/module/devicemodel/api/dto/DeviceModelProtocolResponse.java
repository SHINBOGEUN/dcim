package net.vivans.dcim.module.devicemodel.api.dto;

import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelProtocol;

public record DeviceModelProtocolResponse(
        Integer id,
        Integer protocolTypeId,
        String protocolCode,
        String protocolName,
        boolean isDefault,
        Integer sortOrder
) {

    public static DeviceModelProtocolResponse from(DeviceModelProtocol protocol) {
        return new DeviceModelProtocolResponse(
                protocol.getId(),
                protocol.getProtocolType().getId(),
                protocol.getProtocolType().getCode(),
                protocol.getProtocolType().getName(),
                protocol.isDefault(),
                protocol.getSortOrder()
        );
    }
}
