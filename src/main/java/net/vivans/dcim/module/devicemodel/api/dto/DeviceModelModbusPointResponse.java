package net.vivans.dcim.module.devicemodel.api.dto;

import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelModbusPoint;
import net.vivans.dcim.module.devicemodel.domain.model.ModbusByteOrder;
import net.vivans.dcim.module.devicemodel.domain.model.ModbusDataType;
import net.vivans.dcim.module.devicemodel.domain.model.ModbusRegisterType;
import org.antlr.v4.runtime.misc.IntegerList;

public record DeviceModelModbusPointResponse(
        Integer id,
        Integer modelId,
        Integer protocolId,
        String name,
        ModbusRegisterType registerType,
        ModbusDataType dataType,
        ModbusByteOrder byteOrder,
        Integer address,
        boolean requiresInstance,
        Double scale,
        String unit,
        boolean enabled
) {

    public static DeviceModelModbusPointResponse from(DeviceModelModbusPoint point) {
        return new DeviceModelModbusPointResponse(
                point.getId(),
                point.getModelProtocol().getId(),
                point.getModelProtocol().getId(),
                point.getName(),
                point.getRegisterType(),
                point.getDataType(),
                point.getByteOrder(),
                point.getAddress(),
                point.isRequiresInstance(),
                point.getScale(),
                point.getUnit(),
                point.isEnabled()
        );
    }
}
