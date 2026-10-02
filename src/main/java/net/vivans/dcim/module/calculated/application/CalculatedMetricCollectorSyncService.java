package net.vivans.dcim.module.calculated.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.collectortask.infrastructure.collector.CollectorJobClient;
import net.vivans.dcim.module.device.domain.model.DeviceProtocolEndpoint;
import net.vivans.dcim.module.device.domain.model.DeviceSnmpInstance;
import net.vivans.dcim.module.device.domain.model.DeviceEndpointModbus;
import net.vivans.dcim.module.device.domain.model.DeviceModbusReading;
import net.vivans.dcim.module.device.domain.repository.DeviceProtocolEndpointRepository;
import net.vivans.dcim.module.device.domain.repository.DeviceSnmpInstanceRepository;
import net.vivans.dcim.module.device.domain.repository.DeviceEndpointModbusRepository;
import net.vivans.dcim.module.device.domain.repository.DeviceModbusReadingRepository;
import net.vivans.dcim.module.device.infrastructure.persistence.DeviceModbusBitFieldRepository;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelSnmpPoint;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelModbusPoint;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelSnmpPointRepository;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelModbusPointRepository;
import net.vivans.dcim.module.calculated.domain.model.*;
import net.vivans.dcim.module.calculated.domain.repository.CalculatedMetricRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Slf4j
@Service @RequiredArgsConstructor
public class CalculatedMetricCollectorSyncService {
 private final CollectorJobClient client; private final ObjectMapper mapper; private final DeviceModelSnmpPointRepository points; private final DeviceProtocolEndpointRepository endpoints; private final DeviceSnmpInstanceRepository instances; private final CalculatedMetricRepository definitions;
 private final DeviceModbusReadingRepository readings; private final DeviceModelModbusPointRepository modbusPoints; private final DeviceEndpointModbusRepository modbusEndpoints;
 private final DeviceModbusBitFieldRepository bitFields;
 public void sync(CalculatedMetric d){
  boolean hasDisabledSource=hasDisabledSource(d);
  String action=d.isCollectionEnabled()&&!hasDisabledSource?"UPSERT":"DELETE";
  if(!client.isEnabled()){log.info("[COLLECTOR_SYNC_SKIP] type=CALCULATED action={} definitionId={} reason=CLIENT_DISABLED",action,d.getId());return;}
  log.info("[COLLECTOR_SYNC_START] type=CALCULATED action={} definitionId={} version={}",action,d.getId(),d.getConfigVersion());
  try{
   if(!d.isCollectionEnabled()||hasDisabledSource){client.deleteCalculated(d.getId());log.info("[COLLECTOR_SYNC_END] type=CALCULATED action=DELETE definitionId={} reason={}",d.getId(),hasDisabledSource?"DISABLED_SOURCE":"COLLECTION_DISABLED");return;}
   client.upsertCalculated(d.getId(),mapper.writeValueAsString(calculatedSpec(d)));
   log.info("[COLLECTOR_SYNC_END] type=CALCULATED action=UPSERT definitionId={} sourceCount={}",d.getId(),d.calculatedSources().size());
  } catch(Exception e){
   log.warn("[COLLECTOR_SYNC_ERROR] type=CALCULATED action={} definitionId={} exception={} message={}",action,d.getId(),e.getClass().getSimpleName(),e.getMessage());
   throw new IllegalStateException("Calculated metric collector sync failed: "+e.getMessage(),e);
  }
 }
 public void remove(Integer id){
  if(!client.isEnabled()){log.info("[COLLECTOR_SYNC_SKIP] type=CALCULATED action=DELETE definitionId={} reason=CLIENT_DISABLED",id);return;}
  log.info("[COLLECTOR_SYNC_START] type=CALCULATED action=DELETE definitionId={}",id);
  try{client.deleteCalculated(id);log.info("[COLLECTOR_SYNC_END] type=CALCULATED action=DELETE definitionId={}",id);}
  catch(Exception e){log.warn("[COLLECTOR_SYNC_ERROR] type=CALCULATED action=DELETE definitionId={} exception={} message={}",id,e.getClass().getSimpleName(),e.getMessage());throw new IllegalStateException("Calculated metric collector delete failed: "+e.getMessage(),e);}
 }
 @Transactional(readOnly = true)
 public void repushActiveDefinitions(){if(!client.isEnabled())return;for(CalculatedMetric definition:definitions.findAllByCollectionEnabled(true)){try{sync(definition);log.info("calculated collector job repushed: definitionId={}",definition.getId());}catch(Exception e){log.warn("calculated collector job repush failed: definitionId={}, reason={}",definition.getId(),e.getMessage());}}}
 private boolean hasDisabledSource(CalculatedMetric definition){return definition.calculatedSources().stream().anyMatch(source->!source.device().isEnabled());}

 public void validateCalculatedSources(CalculatedMetric definition) {
  if (definition.getFormula() == null) throw new IllegalArgumentException("calculated metric formula is required");
  calculatedSpec(definition);
 }

 private CalculatedSpec calculatedSpec(CalculatedMetric definition) {
  List<CalculatedSource> resolved = new ArrayList<>();
  for (CalculatedMetric.CalculatedSourceDefinition source : definition.calculatedSources()) {
   if ("snmp".equals(source.protocol())) resolved.add(snmpCalculatedSource(source));
   else if ("modbus".equals(source.protocol())) resolved.add(modbusCalculatedSource(source));
   else throw new IllegalArgumentException("calculated source supports only SNMP/Modbus");
  }
  return new CalculatedSpec(definition.getId(), definition.getConfigVersion(), definition.getCalculationCron(),
          "public", 2000, 1, resolved, definition.getFormula());
 }

 private CalculatedSource snmpCalculatedSource(CalculatedMetric.CalculatedSourceDefinition source) {
  DeviceModelSnmpPoint point = points.findAllEnabledByDeviceModelIds(Set.of(source.device().getDeviceModel().getId()))
          .stream().filter(item -> item.getName().equalsIgnoreCase(source.pointName()))
          .findFirst().orElseThrow(() -> new IllegalArgumentException("enabled SNMP point not found: " + source.pointName()));
  DeviceProtocolEndpoint endpoint = endpoints.findAllByDeviceIdOrderByIdAsc(source.device().getId()).stream()
          .filter(item -> item.isEnabled() && "snmp".equalsIgnoreCase(item.getProtocolType().getCode()))
          .findFirst().orElseThrow(() -> new IllegalArgumentException("enabled SNMP endpoint not found: " + source.device().getId()));
  Integer instance = instances.findByEndpointId(endpoint.getId()).map(DeviceSnmpInstance::getInstanceId).orElse(null);
  String oid = point.resolveOid(instance);
  if (oid == null) throw new IllegalArgumentException("SNMP instance required: " + source.device().getId());
  return new CalculatedSource(source.device().getId(), endpoint.getHost(), endpoint.getPort(), source.pointName(),
          oid, point.getScale(), source.alias(), "snmp", null, null);
 }

 private CalculatedSource modbusCalculatedSource(CalculatedMetric.CalculatedSourceDefinition source) {
  DeviceModbusReading reading = readings.findAllByTargetDeviceIdOrderByIdAsc(source.device().getId()).stream()
          .filter(item -> item.isEnabled() && (item.getPointName().equalsIgnoreCase(source.pointName())
                  || bitFields.findAllByReading_IdOrderByIdAsc(item.getId()).stream()
                      .anyMatch(field -> field.getPointName().equalsIgnoreCase(source.pointName()))))
          .findFirst().orElse(null);
  DeviceModelModbusPoint point;
  DeviceProtocolEndpoint endpoint;
  Integer unitId;
  Integer address;
  if (reading != null) {
   point = reading.getPoint();
   endpoint = reading.getEndpointModbus().getEndpoint();
   unitId = reading.getUnitId();
   address = reading.getAddress();
  } else {
   point = modbusPoints.findAllEnabledByDeviceModelIds(Set.of(source.device().getDeviceModel().getId())).stream()
           .filter(item -> item.getName().equalsIgnoreCase(source.pointName()) && !item.isRequiresInstance())
           .findFirst().orElseThrow(() -> new IllegalArgumentException("enabled Modbus reading/point not found: " + source.pointName()));
   endpoint = endpoints.findAllByDeviceIdOrderByIdAsc(source.device().getId()).stream()
           .filter(item -> item.isEnabled() && "modbus".equalsIgnoreCase(item.getProtocolType().getCode()))
           .findFirst().orElseThrow(() -> new IllegalArgumentException("enabled Modbus endpoint not found: " + source.device().getId()));
   DeviceEndpointModbus modbus = modbusEndpoints.findByEndpointId(endpoint.getId())
           .orElseThrow(() -> new IllegalArgumentException("Modbus unit ID is required: " + source.device().getId()));
   unitId = modbus.getUnitId();
   address = point.getAddress();
  }
  if (!endpoint.isEnabled() || !point.isEnabled() || unitId == null || address == null)
   throw new IllegalArgumentException("Modbus formula source is disabled or incomplete: " + source.pointName());
  List<ModbusBitField> fields = reading == null ? List.of() : bitFields.findAllByReading_IdOrderByIdAsc(reading.getId())
          .stream().filter(field -> field.getPointName().equalsIgnoreCase(source.pointName()))
          .map(field -> new ModbusBitField(field.getPointName(), field.getBitOffset(), field.getBitWidth(),
                  field.getValueMap(), field.getUnmappedValue())).toList();
  ModbusPoint modbusPoint = new ModbusPoint(reading == null ? source.pointName() : reading.getPointName(), point.getRegisterType().name(), address,
          point.getDataType().name(), point.getByteOrder() == null ? null : point.getByteOrder().name(),
          point.getScale(), point.getOffset(), fields);
  return new CalculatedSource(source.device().getId(), endpoint.getHost(), endpoint.getPort(), source.pointName(),
          null, null, source.alias(), "modbus", unitId, modbusPoint);
 }

 record CalculatedSpec(Integer calculatedMetricId,Integer configVersion,String cronExpression,String community,
                       int timeoutMs,int retries,List<CalculatedSource> sources,String formula) {}
 record CalculatedSource(Integer deviceId,String host,int port,String pointName,String oid,Double scale,
                         String alias,String protocol,Integer unitId,ModbusPoint modbusPoint) {}
 record ModbusPoint(String name,String registerType,Integer address,String dataType,String byteOrder,
                    Double scale,Double offset,List<ModbusBitField> bitFields) {}
 record ModbusBitField(String name,int bitOffset,int bitWidth,Map<String,Long> valueMap,Long unmappedValue) {}
}
