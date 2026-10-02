package net.vivans.dcim.module.calculated.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.collectortask.infrastructure.collector.CollectorCalculatedJobResponse;
import net.vivans.dcim.module.collectortask.infrastructure.collector.CollectorJobClient;
import net.vivans.dcim.module.calculated.api.dto.CalculatedMetricStatusResponse;
import net.vivans.dcim.module.calculated.api.dto.CalculatedMetricResponse;
import net.vivans.dcim.module.calculated.domain.model.CalculatedMetric;
import net.vivans.dcim.module.calculated.domain.repository.CalculatedMetricRepository;
import net.vivans.dcim.module.query.domain.PointQuery;
import net.vivans.dcim.module.query.domain.CalculatedMetricLastPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CalculatedMetricStatusService {
    private static final Duration RESULT_LOOKBACK = Duration.ofDays(30);

    private final CalculatedMetricRepository definitions;
    private final CollectorJobClient collector;
    private final PointQuery pointQuery;

    @Transactional(readOnly = true)
    public CalculatedMetricStatusResponse getStatus(Integer id) {
        CalculatedMetric definition = definitions.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Calculated metric not found: " + id));
        if (definition.getFormula() == null) {
            throw new EntityNotFoundException("Calculated metric not found: " + id);
        }

        CollectorCalculatedJobResponse job = null;
        String executionStatus;
        if (!definition.isCollectionEnabled()) {
            executionStatus = "STOPPED";
        } else if (!collector.isEnabled()) {
            executionStatus = "COLLECTOR_UNAVAILABLE";
        } else {
            try {
                job = collector.calculatedJobStatus(id);
                if (job == null) executionStatus = "NOT_SYNCED";
                else if (job.configVersion() == null || job.configVersion() != definition.getConfigVersion()) {
                    executionStatus = "NOT_SYNCED";
                    job = null;
                } else if (job.running()) executionStatus = "RUNNING";
                else if (job.consecutiveFailureCount() > 0) executionStatus = "FAILING";
                else if (job.lastSuccessAt() == null) executionStatus = "WAITING_FIRST_RUN";
                else executionStatus = "NORMAL";
            } catch (RuntimeException exception) {
                executionStatus = "COLLECTOR_UNAVAILABLE";
            }
        }

        Optional<CalculatedMetricLastPoint> latest;
        String storageStatus;
        try {
            latest = pointQuery.findLastCalculated(id, definition.getConfigVersion(), RESULT_LOOKBACK);
            storageStatus = latest.isPresent() ? "SAVED" : "NO_RECENT_RESULT";
        } catch (RuntimeException exception) {
            latest = Optional.empty();
            storageStatus = "INFLUX_UNAVAILABLE";
        }
        CalculatedMetricLastPoint last = latest.orElse(null);
        return new CalculatedMetricStatusResponse(CalculatedMetricResponse.from(definition), executionStatus,
                job != null && job.running(), job == null ? null : job.lastSuccessAt(),
                job == null ? null : job.lastFailureAt(), job == null ? 0 : job.consecutiveFailureCount(),
                job == null ? null : job.lastFailureReason(), storageStatus,
                last == null ? null : last.time(), last == null ? null : last.value(),
                last == null ? java.util.Map.of() : last.inputs());
    }
}
