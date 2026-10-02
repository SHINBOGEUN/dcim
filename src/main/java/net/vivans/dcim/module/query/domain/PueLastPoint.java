package net.vivans.dcim.module.query.domain;

import java.time.Instant;
import java.util.Map;

public record PueLastPoint(
        double value,
        Instant time,
        Map<String, Double> inputs
) {
}
