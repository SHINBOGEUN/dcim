package net.vivans.dcim.module.pue.api.dto;
import net.vivans.dcim.module.pue.domain.model.*; import java.util.List;
public record PueDefinitionResponse(Integer id,String name,String calculationCron,boolean collectionEnabled,int configVersion,String collectorJobId,List<Source> sources,List<DeviceGroup> deviceGroups,String formula,String resultUnit) {
 public record Source(Integer deviceId,String deviceName,String role,String pointName,String alias,String protocol) {}
 public record DeviceGroup(Integer deviceGroupId,String deviceGroupName,String role,String pointName,int deviceCount) {}
 public static PueDefinitionResponse from(PueDefinition d){return new PueDefinitionResponse(d.getId(),d.getName(),d.getCalculationCron(),d.isCollectionEnabled(),d.getConfigVersion(),d.getCollectorJobId(),d.getSources().stream().map(s->new Source(s.getDevice().getId(),s.getDevice().getName(),s.getRole()==null?null:s.getRole().name(),s.getPointName(),s.getAlias(),s.getProtocol())).toList(),d.getDeviceGroups().stream().map(g->new DeviceGroup(g.getDeviceGroup().getId(),g.getDeviceGroup().getName(),g.getRole().name(),g.getPointName(),g.getDeviceGroup().getDevices().size())).toList(),d.getFormula(),d.getResultUnit());}
}
