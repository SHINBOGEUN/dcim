package net.vivans.dcim.module.pue.api.dto;

import net.vivans.dcim.module.pue.domain.model.PueDefinition;
import java.util.List;

public record PueDefinitionResponse(Integer id, String name, String calculationCron,
                                    boolean collectionEnabled, int configVersion,
                                    String collectorJobId, List<Source> sources,
                                    String formula, String resultUnit) {
    public record Source(Integer deviceId, String deviceName, String pointName,
                         String alias, String protocol) {}

    public static PueDefinitionResponse from(PueDefinition definition) {
        return new PueDefinitionResponse(definition.getId(), definition.getName(),
                definition.getCalculationCron(), definition.isCollectionEnabled(),
                definition.getConfigVersion(), definition.getCollectorJobId(),
                definition.getSources().stream().map(source -> new Source(
                        source.getDevice().getId(), source.getDevice().getName(),
                        source.getPointName(), source.getAlias(), source.getProtocol())).toList(),
                definition.getFormula(), definition.getResultUnit());
    }
}
