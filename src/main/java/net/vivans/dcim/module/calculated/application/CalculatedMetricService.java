package net.vivans.dcim.module.calculated.application;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.vivans.dcim.module.device.domain.model.Device;
import net.vivans.dcim.module.device.application.DeviceMeasurementSourceCatalog;
import net.vivans.dcim.module.device.domain.repository.DeviceRepository;
import net.vivans.dcim.module.device.domain.repository.PageWidgetRepository;
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
    private final DeviceMeasurementSourceCatalog sourceCatalog;
    private final PageWidgetRepository pageWidgetRepository;
    private final CalculatedMetricCollectorSyncService collectorSyncService;

    public record SourceOption(Integer deviceId, String deviceName, String protocol, String pointName, String unit) {}

    public List<SourceOption> availableCalculatedSources() {
        return sourceCatalog.availableSources().stream()
                .map(source -> new SourceOption(source.deviceId(), source.deviceName(),
                        source.protocol(), source.pointName(), source.unit()))
                .distinct().toList();
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
