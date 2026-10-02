package net.vivans.dcim.module.calculated.application;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.vivans.dcim.module.device.domain.model.Device;
import net.vivans.dcim.module.device.domain.repository.DeviceRepository;
import net.vivans.dcim.module.device.domain.repository.DeviceModbusReadingRepository;
import net.vivans.dcim.module.device.domain.repository.DeviceProtocolEndpointRepository;
import net.vivans.dcim.module.device.domain.repository.DeviceEndpointModbusRepository;
import net.vivans.dcim.module.device.infrastructure.persistence.DeviceModbusBitFieldRepository;
import net.vivans.dcim.module.device.domain.repository.PageWidgetRepository;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelSnmpPointRepository;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelModbusPointRepository;
import net.vivans.dcim.module.calculated.api.dto.*;
import net.vivans.dcim.module.calculated.domain.model.*;
import net.vivans.dcim.module.calculated.domain.repository.CalculatedMetricRepository;
import net.vivans.dcim.shared.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalculatedMetricService {
    private final CalculatedMetricRepository repository;
    private final DeviceRepository deviceRepository;
    private final DeviceModelSnmpPointRepository pointRepository;
    private final PageWidgetRepository pageWidgetRepository;
    private final CalculatedMetricCollectorSyncService collectorSyncService;
    private final DeviceModbusReadingRepository modbusReadingRepository;
    private final DeviceProtocolEndpointRepository endpointRepository;
    private final DeviceEndpointModbusRepository modbusEndpointRepository;
    private final DeviceModelModbusPointRepository modbusPointRepository;
    private final DeviceModbusBitFieldRepository bitFieldRepository;

    public record SourceOption(Integer deviceId, String deviceName, String protocol, String pointName, String unit) {}

    public List<SourceOption> availableCalculatedSources() {
        List<SourceOption> result = new ArrayList<>();
        for (Device device : deviceRepository.findAllEnabled()) {
            Integer deviceId = device.getId();
            var endpoints = endpointRepository.findAllByDeviceIdOrderByIdAsc(deviceId);
            if (endpoints.stream().anyMatch(endpoint -> endpoint.isEnabled()
                    && "snmp".equalsIgnoreCase(endpoint.getProtocolType().getCode()))) {
                pointRepository.findAllEnabledByDeviceModelIds(Set.of(device.getDeviceModel().getId()))
                        .forEach(point -> result.add(new SourceOption(deviceId, device.getName(), "snmp",
                                point.getName(), point.getUnit())));
            }
            modbusReadingRepository.findAllByTargetDeviceIdOrderByIdAsc(deviceId).stream()
                    .filter(reading -> reading.isEnabled() && reading.getPoint().isEnabled()
                            && reading.getEndpointModbus().getEndpoint().isEnabled())
                    .forEach(reading -> {
                        result.add(new SourceOption(deviceId, device.getName(), "modbus",
                                reading.getPointName(), reading.getPoint().getUnit()));
                        bitFieldRepository.findAllByReading_IdOrderByIdAsc(reading.getId()).forEach(field ->
                                result.add(new SourceOption(deviceId, device.getName(), "modbus",
                                        field.getPointName(), null)));
                    });
            endpoints.stream().filter(endpoint -> endpoint.isEnabled()
                    && "modbus".equalsIgnoreCase(endpoint.getProtocolType().getCode())
                    && modbusEndpointRepository.findByEndpointId(endpoint.getId())
                        .map(modbus -> modbus.getUnitId() != null).orElse(false))
                    .findFirst().ifPresent(endpoint -> modbusPointRepository
                            .findAllEnabledByDeviceModelIds(Set.of(device.getDeviceModel().getId())).stream()
                            .filter(point -> !point.isRequiresInstance())
                            .forEach(point -> result.add(new SourceOption(deviceId, device.getName(), "modbus",
                                    point.getName(), point.getUnit()))));
        }
        return result.stream().distinct().toList();
    }

    public List<CalculatedMetricResponse> listCalculated() {
        return repository.findAll().stream().map(CalculatedMetricResponse::from).toList();
    }

    @Transactional
    public CalculatedMetricResponse createCalculated(CalculatedMetricRequest request) {
        if (repository.existsByName(request.name().trim())) throw new ConflictException("calculated metric name already exists");
        CalculatedMetric definition = CalculatedMetric.createCalculated(request.name(),
                request.calculationCron(), request.collectionEnabled() == null || request.collectionEnabled(),
                request.formula(), request.resultUnit(), calculatedSources(request.sources()));
        collectorSyncService.validateCalculatedSources(definition);
        definition = repository.save(definition);
        collectorSyncService.sync(definition);
        return CalculatedMetricResponse.from(definition);
    }

    @Transactional
    public CalculatedMetricResponse updateCalculated(Integer id, CalculatedMetricRequest request) {
        CalculatedMetric definition = find(id);
        if (repository.existsByNameAndIdNot(request.name().trim(), id)) throw new ConflictException("calculated metric name already exists");
        definition.updateCalculated(request.name(), request.calculationCron(), request.formula(),
                request.resultUnit(), calculatedSources(request.sources()));
        collectorSyncService.validateCalculatedSources(definition);
        if (request.collectionEnabled() != null) definition.setCollectionEnabled(request.collectionEnabled());
        collectorSyncService.sync(definition);
        return CalculatedMetricResponse.from(definition);
    }

    private List<CalculatedMetric.CalculatedSourceDefinition> calculatedSources(List<CalculatedMetricRequest.Source> sources) {
        if (sources == null) throw new IllegalArgumentException("sources are required");
        List<CalculatedMetric.CalculatedSourceDefinition> result = new ArrayList<>();
        for (CalculatedMetricRequest.Source source : sources) {
            Device device = deviceRepository.findById(source.deviceId())
                    .orElseThrow(() -> new EntityNotFoundException("Device not found: " + source.deviceId()));
            if (!device.isEnabled()) throw new IllegalArgumentException("source device is disabled: " + source.deviceId());
            result.add(new CalculatedMetric.CalculatedSourceDefinition(device, source.alias(), source.pointName(),
                    source.protocol().toLowerCase(Locale.ROOT)));
        }
        return result;
    }

    @Transactional
    public CalculatedMetricResponse setCollectionEnabled(Integer id, boolean enabled) {
        CalculatedMetric definition = find(id);
        definition.setCollectionEnabled(enabled);
        definition = repository.save(definition);
        collectorSyncService.sync(definition);
        log.info("[CALCULATED_METRIC] action=COLLECTION_TOGGLE definitionId={} enabled={}", id, enabled);
        return CalculatedMetricResponse.from(definition);
    }

    @Transactional
    public void delete(Integer id) {
        CalculatedMetric definition = find(id);
        if (pageWidgetRepository.existsByCalculatedMetricId(id)) {
            throw new ConflictException("Calculated metric is used by a widget. Delete the widget first.");
        }
        collectorSyncService.remove(id);
        repository.delete(definition);
        log.info("[CALCULATED_METRIC] action=DELETE definitionId={} name={}", id, definition.getName());
    }

    private CalculatedMetric find(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Calculated metric not found: " + id));
    }

}
