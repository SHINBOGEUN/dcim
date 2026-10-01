package net.vivans.dcim.module.device.api.dto;

import net.vivans.dcim.module.device.domain.model.DeviceModbusBitField;

import java.util.Map;

public record DeviceModbusBitFieldResponse(
        Integer id,
        String pointName,
        int bitOffset,
        int bitWidth,
        Map<String, Long> valueMap,
        Long unmappedValue
) {
    public static DeviceModbusBitFieldResponse from(DeviceModbusBitField field) {
        return new DeviceModbusBitFieldResponse(field.getId(), field.getPointName(), field.getBitOffset(),
                field.getBitWidth(), field.getValueMap(), field.getUnmappedValue());
    }
}
