package net.vivans.dcim.module.devicemodel.api.dto;

import java.util.List;

public record DeviceModelMeasurementPointOptionsResponse(
        Integer modelId,
        String modelName,
        List<PointOption> points
) {
    public record PointOption(
            Integer pointId,
            String protocol,
            String pointName,
            String unit,
            String valueType,
            Boolean requiresInstance,
            boolean collectionEnabled
    ) {
    }
}
