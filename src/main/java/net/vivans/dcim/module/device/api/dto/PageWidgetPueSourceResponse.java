package net.vivans.dcim.module.device.api.dto;

import net.vivans.dcim.module.device.domain.model.PageWidgetPueSource;
import net.vivans.dcim.module.pue.domain.model.PueDefinitionSource;
import net.vivans.dcim.module.pue.domain.model.PueDefinition;

public record PageWidgetPueSourceResponse(Integer deviceId, String deviceName, String role, String pointName,
                                          String alias, String protocol) {
    public static PageWidgetPueSourceResponse from(PageWidgetPueSource source) {
        return new PageWidgetPueSourceResponse(source.getDevice().getId(), source.getDevice().getName(),
                source.getRole().name(), source.getPointName(), null, null);
    }

    public static PageWidgetPueSourceResponse from(PueDefinitionSource source) {
        return new PageWidgetPueSourceResponse(source.getDevice().getId(), source.getDevice().getName(),
                source.getRole() == null ? null : source.getRole().name(), source.getPointName(),
                source.getAlias(), source.getProtocol());
    }

    public static PageWidgetPueSourceResponse from(PueDefinition.SourceDefinition source) {
        return new PageWidgetPueSourceResponse(source.device().getId(), source.device().getName(),
                source.role().name(), source.pointName(), null, null);
    }
    public static PageWidgetPueSourceResponse from(PueDefinition.CalculatedSourceDefinition source) {
        return new PageWidgetPueSourceResponse(source.device().getId(), source.device().getName(), null,
                source.pointName(), source.alias(), source.protocol());
    }
}
