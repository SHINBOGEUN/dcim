package net.vivans.dcim.module.devicemodel.api.dto;

import java.util.List;

public record DeviceModelMeasurementPointOptionsResponse(
        Integer modelId,
        String modelName,
        List<PointOption> points
) {
    public record PointOption(
            Integer pointId,
            Integer protocolTypeId,
            String protocol,
            String pointName,
            String unit,
            String valueType,
            Integer dataPointTypeId,
            String dataPointType,
            Integer categoryCodeId,
            String categoryCode,
            String categoryName,
            Boolean requiresInstance,
            boolean collectionEnabled
    ) {
    }
}
