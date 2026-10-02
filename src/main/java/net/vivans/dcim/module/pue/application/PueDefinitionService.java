package net.vivans.dcim.module.pue.application;
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
import net.vivans.dcim.module.pue.api.dto.*;
import net.vivans.dcim.module.pue.domain.model.*;
import net.vivans.dcim.module.pue.domain.repository.PueDefinitionRepository;
import net.vivans.dcim.shared.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PueDefinitionService {
    private final PueDefinitionRepository repository;
    private final DeviceRepository deviceRepository;
    private final DeviceModelSnmpPointRepository pointRepository;
    private final PageWidgetRepository pageWidgetRepository;
    private final PueCollectorSyncService collectorSyncService;
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

    public List<PueDefinitionResponse> listCalculated() {
        return repository.findAll().stream().map(PueDefinitionResponse::from).toList();
    }

    @Transactional
    public PueDefinitionResponse createCalculated(CalculatedMetricRequest request) {
        if (repository.existsByName(request.name().trim())) throw new ConflictException("calculated metric name already exists");
        PueDefinition definition = PueDefinition.createCalculated(request.name(),
                request.calculationCron(), request.collectionEnabled() == null || request.collectionEnabled(),
                request.formula(), request.resultUnit(), calculatedSources(request.sources()));
        collectorSyncService.validateCalculatedSources(definition);
        definition = repository.save(definition);
        collectorSyncService.sync(definition);
        return PueDefinitionResponse.from(definition);
    }

    @Transactional
    public PueDefinitionResponse updateCalculated(Integer id, CalculatedMetricRequest request) {
        PueDefinition definition = find(id);
        if (repository.existsByNameAndIdNot(request.name().trim(), id)) throw new ConflictException("calculated metric name already exists");
        definition.updateCalculated(request.name(), request.calculationCron(), request.formula(),
                request.resultUnit(), calculatedSources(request.sources()));
        collectorSyncService.validateCalculatedSources(definition);
        if (request.collectionEnabled() != null) definition.setCollectionEnabled(request.collectionEnabled());
        collectorSyncService.sync(definition);
        return PueDefinitionResponse.from(definition);
    }

    private List<PueDefinition.CalculatedSourceDefinition> calculatedSources(List<CalculatedMetricRequest.Source> sources) {
        if (sources == null) throw new IllegalArgumentException("sources are required");
        List<PueDefinition.CalculatedSourceDefinition> result = new ArrayList<>();
        for (CalculatedMetricRequest.Source source : sources) {
            Device device = deviceRepository.findById(source.deviceId())
                    .orElseThrow(() -> new EntityNotFoundException("Device not found: " + source.deviceId()));
            if (!device.isEnabled()) throw new IllegalArgumentException("source device is disabled: " + source.deviceId());
            result.add(new PueDefinition.CalculatedSourceDefinition(device, source.alias(), source.pointName(),
                    source.protocol().toLowerCase(Locale.ROOT)));
        }
        return result;
    }

    @Transactional
    public PueDefinitionResponse setCollectionEnabled(Integer id, boolean enabled) {
        PueDefinition definition = find(id);
        definition.setCollectionEnabled(enabled);
        definition = repository.save(definition);
        collectorSyncService.sync(definition);
        log.info("[CALCULATED_METRIC] action=COLLECTION_TOGGLE definitionId={} enabled={}", id, enabled);
        return PueDefinitionResponse.from(definition);
    }

    @Transactional
    public void delete(Integer id) {
        PueDefinition definition = find(id);
        if (pageWidgetRepository.existsByPueDefinitionId(id)) {
            throw new ConflictException("Calculated metric is used by a widget. Delete the widget first.");
        }
        collectorSyncService.remove(id);
        repository.delete(definition);
        log.info("[CALCULATED_METRIC] action=DELETE definitionId={} name={}", id, definition.getName());
    }

    private PueDefinition find(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Calculated metric not found: " + id));
    }

}
