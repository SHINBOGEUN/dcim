package net.vivans.dcim.module.query.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CalculatedMetricTrendPointResponse(
        Instant time,
        BigDecimal value
) {
}
