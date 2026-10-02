package net.vivans.dcim.module.pue.api.dto;

import java.time.Instant;
import java.util.Map;

public record CalculatedMetricStatusResponse(
        PueDefinitionResponse definition,
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
