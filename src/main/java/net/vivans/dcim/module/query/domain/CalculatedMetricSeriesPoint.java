package net.vivans.dcim.module.query.domain;

import java.time.Instant;

public record CalculatedMetricSeriesPoint(
        double value,
        Instant time
) {
}
