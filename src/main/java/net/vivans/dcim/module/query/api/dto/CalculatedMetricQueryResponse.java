package net.vivans.dcim.module.query.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CalculatedMetricQueryResponse(
        BigDecimal value,
        String unit,
        String rangePreset,
        Instant start,
        Instant end,
        boolean complete,
        String calculationStatus,
        List<CalculatedMetricTrendPointResponse> trend,
        WidgetDataStatusResponse dataStatus,
        String formula,
        Map<String, Double> inputs
) {
}
