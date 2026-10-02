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
import net.vivans.dcim.module.devicegroup.domain.model.DeviceGroup;
import net.vivans.dcim.module.devicegroup.domain.repository.DeviceGroupRepository;
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
    private final DeviceGroupRepository deviceGroupRepository;
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

    public List<PueDefinitionResponse> list() {
        return repository.findAll().stream().map(PueDefinitionResponse::from).toList();
    }

    public List<PueDefinitionResponse> listCalculated() {
        return repository.findAll().stream().filter(definition -> definition.getFormula() != null)
                .map(PueDefinitionResponse::from).toList();
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
        if (definition.getFormula() == null) throw new IllegalArgumentException("legacy PUE definition cannot be edited as a calculated metric");
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
    public PueDefinitionResponse create(PueDefinitionRequest request) {
        if (repository.existsByName(request.name().trim())) {
            throw new ConflictException("PUE definition name already exists");
        }
        Configuration configuration = configuration(request);
        PueDefinition definition = repository.save(PueDefinition.create(
                request.name(),
                request.calculationCron(),
                request.collectionEnabled() == null || request.collectionEnabled(),
                configuration.sources(), configuration.deviceGroups()
        ));
        collectorSyncService.sync(definition);
        log.info("[PUE_DEFINITION] action=CREATE definitionId={} name={} sourceCount={} enabled={} cron={}",
                definition.getId(), definition.getName(), definition.resolvedSources().size(),
                definition.isCollectionEnabled(), definition.getCalculationCron());
        return PueDefinitionResponse.from(definition);
    }

    @Transactional
    public PueDefinitionResponse update(Integer id, PueDefinitionRequest request) {
        PueDefinition definition = find(id);
        if (repository.existsByNameAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("PUE definition name already exists");
        }
        Configuration configuration = configuration(request);
        definition.update(request.name(), request.calculationCron(), configuration.sources(), configuration.deviceGroups());
        if (request.collectionEnabled() != null) {
            definition.setCollectionEnabled(request.collectionEnabled());
        }
        definition = repository.save(definition);
        collectorSyncService.sync(definition);
        log.info("[PUE_DEFINITION] action=UPDATE definitionId={} name={} sourceCount={} enabled={} cron={} version={}",
                definition.getId(), definition.getName(), definition.resolvedSources().size(),
                definition.isCollectionEnabled(), definition.getCalculationCron(), definition.getConfigVersion());
        return PueDefinitionResponse.from(definition);
    }

    @Transactional
    public PueDefinitionResponse setCollectionEnabled(Integer id, boolean enabled) {
        PueDefinition definition = find(id);
        definition.setCollectionEnabled(enabled);
        definition = repository.save(definition);
        collectorSyncService.sync(definition);
        log.info("[PUE_DEFINITION] action=COLLECTION_TOGGLE definitionId={} enabled={}", id, enabled);
        return PueDefinitionResponse.from(definition);
    }

    @Transactional
    public void delete(Integer id) {
        PueDefinition definition = find(id);
        if (pageWidgetRepository.existsByPueDefinitionId(id)) {
            throw new ConflictException("PUE definition is used by a widget. Delete the widget or select another PUE definition first.");
        }
        collectorSyncService.remove(id);
        repository.delete(definition);
        log.info("[PUE_DEFINITION] action=DELETE definitionId={} name={}", id, definition.getName());
    }

    private PueDefinition find(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PueDefinition not found: " + id));
    }

    private Configuration configuration(PueDefinitionRequest request) {
        List<PueDefinition.SourceDefinition> all = new ArrayList<>();
        List<PueDefinition.DeviceGroupDefinition> groups = new ArrayList<>();
        boolean sourceConfiguration = hasItems(request.totalSources()) || hasItems(request.coolerSources());
        boolean groupConfiguration = hasItems(request.totalDeviceGroups()) || hasItems(request.coolerDeviceGroups());
        if (sourceConfiguration && groupConfiguration) {
            throw new IllegalArgumentException("PUE source devices and device groups cannot be used together");
        }
        if (!sourceConfiguration && !groupConfiguration) {
            throw new IllegalArgumentException("PUE requires total and cooler device groups");
        }
        if (sourceConfiguration) {
            if (!hasItems(request.totalSources()) || !hasItems(request.coolerSources())) {
                throw new IllegalArgumentException("PUE requires totalSources and coolerSources");
            }
            add(all, request.totalSources(), PueDefinitionSourceRole.total);
            add(all, request.coolerSources(), PueDefinitionSourceRole.cooler);
        } else {
            if (!hasItems(request.totalDeviceGroups()) || !hasItems(request.coolerDeviceGroups())) {
                throw new IllegalArgumentException("PUE requires totalDeviceGroups and coolerDeviceGroups");
            }
            addGroups(groups, request.totalDeviceGroups(), PueDefinitionSourceRole.total);
            addGroups(groups, request.coolerDeviceGroups(), PueDefinitionSourceRole.cooler);
        }
        PueDefinition candidate = PueDefinition.create("PUE validation", null, true, all, groups);
        validatePowerPoints(candidate.resolvedSources());
        return new Configuration(all, groups);
    }

    private void add(List<PueDefinition.SourceDefinition> all,
                     List<PueDefinitionSourceRequest> requests,
                     PueDefinitionSourceRole role) {
        for (PueDefinitionSourceRequest request : requests) {
            Device device = deviceRepository.findById(request.deviceId())
                    .orElseThrow(() -> new EntityNotFoundException("Device not found: " + request.deviceId()));
            if (!device.isEnabled()) {
                throw new IllegalArgumentException("PUE source device is disabled: " + request.deviceId());
            }
            all.add(new PueDefinition.SourceDefinition(device, role, request.pointName()));
        }
    }

    private void addGroups(List<PueDefinition.DeviceGroupDefinition> target,
                           List<PueDefinitionDeviceGroupRequest> requests,
                           PueDefinitionSourceRole role) {
        for (PueDefinitionDeviceGroupRequest request : requests) {
            DeviceGroup group = deviceGroupRepository.findById(request.deviceGroupId())
                    .orElseThrow(() -> new EntityNotFoundException("DeviceGroup not found: " + request.deviceGroupId()));
            if (!group.isEnabled()) {
                throw new IllegalArgumentException("PUE device group is disabled: " + group.getName());
            }
            if (group.getDevices().isEmpty()) {
                throw new IllegalArgumentException("PUE device group has no devices: " + group.getName());
            }
            target.add(new PueDefinition.DeviceGroupDefinition(group, role, request.pointName()));
        }
    }

    private void validatePowerPoints(List<PueDefinition.SourceDefinition> sources) {
        new PuePowerPointValidator(pointRepository).validateDefinitionSources(sources.stream()
                .map(source -> new PuePowerPointValidator.Source(source.device(), source.pointName()))
                .toList());
    }

    private static boolean hasItems(List<?> values) {
        return values != null && !values.isEmpty();
    }

    private record Configuration(List<PueDefinition.SourceDefinition> sources,
                                 List<PueDefinition.DeviceGroupDefinition> deviceGroups) {
    }
}
