package net.vivans.dcim.module.devicemodel.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.devicemodel.api.dto.DeviceModelMeasurementPointOptionsResponse;
import net.vivans.dcim.module.devicemodel.api.dto.DeviceModelMeasurementPointOptionsResponse.PointOption;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelModbusPoint;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelProtocol;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelSnmpPoint;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelModbusPointRepository;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelRepository;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelSnmpPointRepository;
import net.vivans.dcim.module.lora.domain.repository.DeviceModelLoraPointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceModelMeasurementPointQueryService {
    private final DeviceModelRepository deviceModelRepository;
    private final DeviceModelSnmpPointRepository snmpPointRepository;
    private final DeviceModelModbusPointRepository modbusPointRepository;
    private final DeviceModelLoraPointRepository loraPointRepository;

    public DeviceModelMeasurementPointOptionsResponse getMeasurementPoints(Integer modelId) {
        DeviceModel model = deviceModelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("DeviceModel not found: " + modelId));

        List<PointOption> points = new ArrayList<>();
        for (DeviceModelProtocol protocol : model.getProtocols()) {
            String protocolCode = protocol.getProtocolType().getCode();
            if ("snmp".equalsIgnoreCase(protocolCode)) {
                snmpPointRepository.findAllByModelProtocolIdOrderByIdAsc(protocol.getId()).forEach(point ->
                        points.add(new PointOption(
                                point.getId(),
                                "snmp",
                                point.getName(),
                                point.getUnit(),
                                point.getDataPointType() == null ? null : point.getDataPointType().getCode(),
                                point.isRequiresInstance(),
                                point.isEnabled()
                        )));
            } else if ("modbus".equalsIgnoreCase(protocolCode)) {
                modbusPointRepository.findAllByModelProtocolIdOrderByIdAsc(protocol.getId()).forEach(point ->
                        points.add(modbusOption(point)));
            }
        }

        loraPointRepository.findAllByDeviceModelIdOrderByIdAsc(modelId).forEach(point ->
                points.add(new PointOption(
                        point.getId(),
                        "mqtt",
                        point.getPointName(),
                        point.getUnit(),
                        point.getDataPointType().getCode(),
                        null,
                        point.isEnabled()
                )));

        points.sort(Comparator.comparing(PointOption::protocol)
                .thenComparing(PointOption::pointName));

        return new DeviceModelMeasurementPointOptionsResponse(
                model.getId(), model.getName(), List.copyOf(points));
    }

    private static PointOption modbusOption(DeviceModelModbusPoint point) {
        return new PointOption(
                point.getId(),
                "modbus",
                point.getName(),
                point.getUnit(),
                point.getDataType().name(),
                point.isRequiresInstance(),
                point.isEnabled()
        );
    }
}
