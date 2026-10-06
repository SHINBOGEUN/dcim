package net.vivans.dcim.module.devicemodel.api.dto;

import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelSnmpPoint;

public record DeviceModelSnmpPointResponse(
        Integer id,
        Integer modelId,
        Integer protocolId,
        Integer dataPointTypeId,
        String dataPointType,
        Integer categoryCodeId,
        String categoryCode,
        String categoryName,
        String name,
        String oid,
        boolean requiresInstance,
        Integer unitCodeId,
        String unit,
        Double scale,
        boolean enabled
) {

    public static DeviceModelSnmpPointResponse from(DeviceModelSnmpPoint point) {
        return new DeviceModelSnmpPointResponse(
                point.getId(),
                point.getModelProtocol().getDeviceModel().getId(),
                point.getModelProtocol().getId(),
                point.getDataPointType() == null ? null : point.getDataPointType().getId(),
                point.getDataPointType() == null ? null : point.getDataPointType().getCode(),
                point.getCategoryCode() == null ? null : point.getCategoryCode().getId(),
                point.getCategoryCode() == null ? null : point.getCategoryCode().getCode(),
                point.getCategoryCode() == null ? null : point.getCategoryCode().getName(),
                point.getName(),
                point.getOid(),
                point.isRequiresInstance(),
                point.getUnitCode() == null ? null : point.getUnitCode().getId(),
                point.getUnit(),
                point.getScale(),
                point.isEnabled()
        );
    }
}
