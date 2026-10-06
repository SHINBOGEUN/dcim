package net.vivans.dcim.module.device.api.dto;

import java.util.List;

public record DevicePageModelPointSettingsResponse(
        String pageCode,
        Integer modelId,
        String modelName,
        List<PointSetting> points
) {
    public record PointSetting(
            Integer protocolTypeId,
            String protocol,
            Integer pointId,
            String pointName,
            String unit,
            String valueType,
            Integer dataPointTypeId,
            String dataPointType,
            Boolean requiresInstance,
            boolean collectionEnabled,
            boolean visible,
            int sortOrder
    ) {
    }
}
