package net.vivans.dcim.module.device.api.dto;

import net.vivans.dcim.module.calculated.domain.model.CalculatedMetric;

public record PageWidgetCalculatedSourceResponse(Integer deviceId, String deviceName, String pointName,
                                          String alias, String protocol) {
    public static PageWidgetCalculatedSourceResponse from(CalculatedMetric.CalculatedSourceDefinition source) {
        return new PageWidgetCalculatedSourceResponse(source.device().getId(), source.device().getName(),
                source.pointName(), source.alias(), source.protocol());
    }
}
