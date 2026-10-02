package net.vivans.dcim.module.device.api.dto;

import net.vivans.dcim.module.pue.domain.model.PueDefinition;

public record PageWidgetPueSourceResponse(Integer deviceId, String deviceName, String pointName,
                                          String alias, String protocol) {
    public static PageWidgetPueSourceResponse from(PueDefinition.CalculatedSourceDefinition source) {
        return new PageWidgetPueSourceResponse(source.device().getId(), source.device().getName(),
                source.pointName(), source.alias(), source.protocol());
    }
}
