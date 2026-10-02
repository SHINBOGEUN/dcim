package net.vivans.dcim.module.query.domain;

import java.time.Instant;
import java.util.Map;

public record PueLastPoint(
        double value,
        Double totalPower,
        Double coolerPower,
        Instant time,
        Map<String, Double> inputs
) {
    public PueLastPoint(double value, Double totalPower, Double coolerPower, Instant time) {
        this(value, totalPower, coolerPower, time, Map.of());
    }
}
