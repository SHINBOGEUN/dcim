package net.vivans.dcim.module.collectortask.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.vivans.dcim.module.collectortask.domain.model.CollectionTask;
import net.vivans.dcim.module.collectortask.domain.model.CollectionTaskDevice;
import net.vivans.dcim.module.collectortask.domain.model.CollectionTaskGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.device.domain.model.Device;
import net.vivans.dcim.module.device.domain.model.DeviceEndpointModbus;
import net.vivans.dcim.module.device.domain.model.DeviceModbusReading;
import net.vivans.dcim.module.device.domain.model.DeviceProtocolEndpoint;
import net.vivans.dcim.module.device.domain.repository.DeviceEndpointModbusRepository;
import net.vivans.dcim.module.device.domain.repository.DeviceModbusReadingRepository;
import net.vivans.dcim.module.device.infrastructure.persistence.DeviceModbusBitFieldRepository;
import net.vivans.dcim.module.device.domain.repository.DeviceProtocolEndpointRepository;
import net.vivans.dcim.module.device.domain.repository.DeviceSnmpInstanceRepository;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelModbusPoint;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelProtocol;
import net.vivans.dcim.module.devicemodel.domain.model.ModbusByteOrder;
import net.vivans.dcim.module.devicemodel.domain.model.ModbusDataType;
import net.vivans.dcim.module.devicemodel.domain.model.ModbusRegisterType;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelModbusPointRepository;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelSnmpPointRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModbusCollectionGroupSpecServiceTest {

    @Test
    void fixedAndMappedPointsUseSourceEndpointButStoreUnderTheirOwnDeviceIds() throws Exception {
        DeviceModelSnmpPointRepository snmpPoints = mock(DeviceModelSnmpPointRepository.class);
        DeviceModelModbusPointRepository modbusPoints = mock(DeviceModelModbusPointRepository.class);
        DeviceProtocolEndpointRepository endpoints = mock(DeviceProtocolEndpointRepository.class);
        DeviceEndpointModbusRepository endpointSettings = mock(DeviceEndpointModbusRepository.class);
        DeviceModbusReadingRepository readings = mock(DeviceModbusReadingRepository.class);
        CollectionGroupSpecService service = new CollectionGroupSpecService(
                snmpPoints, modbusPoints, endpoints, endpointSettings, readings,
                mock(DeviceModbusBitFieldRepository.class),
                mock(DeviceSnmpInstanceRepository.class), new ObjectMapper());

        CommonCode modbusCode = mock(CommonCode.class);
        when(modbusCode.getCode()).thenReturn("modbus");
        DeviceModel model = mock(DeviceModel.class);
        when(model.getId()).thenReturn(10);
        DeviceModelProtocol protocol = mock(DeviceModelProtocol.class);
        when(protocol.getId()).thenReturn(20);
        when(protocol.getProtocolType()).thenReturn(modbusCode);
        when(model.getProtocols()).thenReturn(List.of(protocol));

        CollectionTask task = mock(CollectionTask.class);
        when(task.getId()).thenReturn(2);
        when(task.getDeviceModel()).thenReturn(model);
        when(task.getScriptType()).thenReturn(modbusCode);
        CollectionTaskGroup group = mock(CollectionTaskGroup.class);
        when(group.getTask()).thenReturn(task);
        when(group.getId()).thenReturn(6);
        when(group.getCronExpression()).thenReturn("0 */1 * * * *");

        Device source = mock(Device.class);
        when(source.getId()).thenReturn(7);
        when(source.getName()).thenReturn("meter");
        when(source.getDeviceModel()).thenReturn(model);
        when(source.isEnabled()).thenReturn(true);
        CollectionTaskDevice mapping = mock(CollectionTaskDevice.class);
        when(mapping.getDevice()).thenReturn(source);
        when(group.getDevices()).thenReturn(List.of(mapping));

        DeviceProtocolEndpoint endpoint = mock(DeviceProtocolEndpoint.class);
        when(endpoint.getId()).thenReturn(11);
        when(endpoint.isEnabled()).thenReturn(true);
        when(endpoint.getProtocolType()).thenReturn(modbusCode);
        when(endpoint.getHost()).thenReturn("192.0.2.10");
        when(endpoint.getPort()).thenReturn(502);
        when(endpoints.findAllByDeviceIdOrderByIdAsc(7)).thenReturn(List.of(endpoint));
        DeviceEndpointModbus settings = mock(DeviceEndpointModbus.class);
        when(settings.getUnitId()).thenReturn(3);
        when(endpointSettings.findByEndpointId(11)).thenReturn(Optional.of(settings));

        DeviceModelModbusPoint fixed = mock(DeviceModelModbusPoint.class);
        when(fixed.isEnabled()).thenReturn(true);
        when(fixed.getName()).thenReturn("TEMPERATURE");
        when(fixed.getAddress()).thenReturn(100);
        when(fixed.getRegisterType()).thenReturn(ModbusRegisterType.HOLDING);
        when(fixed.getDataType()).thenReturn(ModbusDataType.INT16);
        when(fixed.getScale()).thenReturn(0.1);
        when(fixed.getOffset()).thenReturn(-50.0);
        when(fixed.getUnit()).thenReturn("°C");
        DeviceModelModbusPoint mapped = mock(DeviceModelModbusPoint.class);
        when(mapped.isEnabled()).thenReturn(true);
        when(mapped.isRequiresInstance()).thenReturn(true);
        when(mapped.getModelProtocol()).thenReturn(protocol);
        when(mapped.getRegisterType()).thenReturn(ModbusRegisterType.INPUT);
        when(mapped.getDataType()).thenReturn(ModbusDataType.FLOAT32);
        when(mapped.getByteOrder()).thenReturn(ModbusByteOrder.CDAB);
        when(mapped.getUnit()).thenReturn("W");
        when(modbusPoints.findAllByModelProtocolIdOrderByIdAsc(20)).thenReturn(List.of(fixed, mapped));

        Device storage = mock(Device.class);
        when(storage.getId()).thenReturn(101);
        when(storage.isEnabled()).thenReturn(true);
        DeviceModbusReading reading = mock(DeviceModbusReading.class);
        when(reading.isEnabled()).thenReturn(true);
        when(reading.getPoint()).thenReturn(mapped);
        when(reading.getTargetDevice()).thenReturn(storage);
        when(reading.getPointName()).thenReturn("POWER");
        when(reading.getAddress()).thenReturn(200);
        when(reading.getUnitId()).thenReturn(1);
        when(readings.findAllByEndpointIdOrderByIdAsc(11)).thenReturn(List.of(reading));

        CollectionGroupPlan plan = service.generate(group);
        JsonNode json = new ObjectMapper().readTree(service.generateJson(group));
        assertEquals("modbus", json.path("protocol").asText());
        assertEquals(2, json.path("targets").size());
        assertEquals(7, json.path("targets").get(0).path("deviceId").asInt());
        assertEquals(3, json.path("targets").get(0).path("unitId").asInt());
        assertEquals(0.1, json.path("targets").get(0).path("points").get(0).path("scale").asDouble());
        assertEquals(-50.0, json.path("targets").get(0).path("points").get(0).path("offset").asDouble());
        assertEquals(101, json.path("targets").get(1).path("deviceId").asInt());
        assertEquals(1, json.path("targets").get(1).path("unitId").asInt());
        assertEquals("CDAB", json.path("targets").get(1).path("points").get(0).path("byteOrder").asText());
        assertEquals("192.0.2.10", json.path("targets").get(1).path("host").asText());
        assertTrue(plan.includesSource(7));
        assertFalse(plan.includesSource(101));
        assertEquals(List.of("POWER", "TEMPERATURE"), plan.pointNamesForSource(7).stream().sorted().toList());
        assertEquals("W", plan.unitsForSource(7).get("POWER"));
    }
}
