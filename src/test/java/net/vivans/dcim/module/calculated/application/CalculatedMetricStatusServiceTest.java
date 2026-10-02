package net.vivans.dcim.module.calculated.application;

import net.vivans.dcim.module.collectortask.infrastructure.collector.CollectorCalculatedJobResponse;
import net.vivans.dcim.module.collectortask.infrastructure.collector.CollectorJobClient;
import net.vivans.dcim.module.device.domain.model.Device;
import net.vivans.dcim.module.calculated.domain.model.CalculatedMetric;
import net.vivans.dcim.module.calculated.domain.repository.CalculatedMetricRepository;
import net.vivans.dcim.module.query.domain.PointQuery;
import net.vivans.dcim.module.query.domain.CalculatedMetricLastPoint;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CalculatedMetricStatusServiceTest {
    @Test
    void distinguishesCollectorExecutionFromInfluxStorage() {
        CalculatedMetricRepository definitions = mock(CalculatedMetricRepository.class);
        CollectorJobClient collector = mock(CollectorJobClient.class);
        PointQuery points = mock(PointQuery.class);
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(7);
        when(device.getName()).thenReturn("PDU");
        CalculatedMetric definition = CalculatedMetric.createCalculated("efficiency", null, true, "A", "W",
                List.of(new CalculatedMetric.CalculatedSourceDefinition(device, "A", "POWER", "snmp")));
        ReflectionTestUtils.setField(definition, "id", 12);
        when(definitions.findById(12)).thenReturn(Optional.of(definition));
        when(collector.isEnabled()).thenReturn(true);
        Instant failureAt = Instant.parse("2026-10-02T00:00:00Z");
        when(collector.calculatedJobStatus(12)).thenReturn(new CollectorCalculatedJobResponse(
                12, 1, false, null, failureAt, 2, "read timed out"));
        when(points.findLastCalculated(eq(12), eq(1), eq(Duration.ofDays(30))))
                .thenReturn(Optional.of(new CalculatedMetricLastPoint(42,
                        Instant.parse("2026-10-01T00:00:00Z"), Map.of("A", 42D))));

        var status = new CalculatedMetricStatusService(definitions, collector, points).getStatus(12);

        assertThat(status.executionStatus()).isEqualTo("FAILING");
        assertThat(status.storageStatus()).isEqualTo("SAVED");
        assertThat(status.lastValue()).isEqualTo(42D);
        assertThat(status.lastInputs()).containsEntry("A", 42D);
    }
}
