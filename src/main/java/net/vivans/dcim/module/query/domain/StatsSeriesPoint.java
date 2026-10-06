package net.vivans.dcim.module.query.domain;

import java.time.Instant;

public record StatsSeriesPoint(
        Integer deviceId,
        String pointName,
        Double min,
        Double max,
        Double avg,
        Instant time
) {
}
