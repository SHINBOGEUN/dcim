package net.vivans.dcim.module.query.infrastructure.influx;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CalculatedMetricFluxBuilderTest {
    @Test
    void latestStatusUsesCurrentConfigurationVersion() {
        String flux = LastFluxBuilder.buildCalculatedLastQuery("dcim", "dcim_sensor", 12, 3,
                Duration.ofDays(30));
        assertThat(flux).contains("r[\"calculated_metric_id\"] == \"12\"");
        assertThat(flux).contains("r[\"calculated_config_version\"] == \"3\"");
    }

    @Test
    void previousResultLookupExcludesCurrentConfigurationVersion() {
        String flux = LastFluxBuilder.buildCalculatedLastPreviousVersionQuery("dcim", "dcim_sensor", 12,
                3, Duration.ofDays(1));

        assertThat(flux).contains("r[\"calculated_metric_id\"] == \"12\"");
        assertThat(flux).contains("r[\"calculated_config_version\"] != \"3\"");
        assertThat(flux).contains("|> sort(columns: [\"_time\"], desc: true)");
    }

    @Test
    void queriesOnlyStoredCalculatedResults() {
        String flux = CalculatedMetricFluxBuilder.buildSeriesQuery("dcim", "dcim_sensor", 12,
                Instant.parse("2026-10-01T00:00:00Z"), Instant.parse("2026-10-02T00:00:00Z"), "15m");

        assertThat(flux).contains("r[\"metric_kind\"] == \"calculated\"");
        assertThat(flux).contains("r[\"calculated_metric_id\"] == \"12\"");
        assertThat(flux).contains("r[\"_field\"] == \"value\"");
        assertThat(flux).doesNotContain("total_power", "cooler_power");
    }
}
