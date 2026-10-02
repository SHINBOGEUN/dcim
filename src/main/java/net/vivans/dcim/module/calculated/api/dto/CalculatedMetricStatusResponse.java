package net.vivans.dcim.module.calculated.api.dto;

import java.time.Instant;
import java.util.Map;

public record CalculatedMetricStatusResponse(
        CalculatedMetricResponse definition,
        String executionStatus,
        boolean running,
        Instant lastSuccessAt,
        Instant lastFailureAt,
        int consecutiveFailureCount,
        String lastFailureReason,
        String storageStatus,
        Instant lastSavedAt,
        Double lastValue,
        Map<String, Double> lastInputs
) {
}
