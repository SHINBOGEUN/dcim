package net.vivans.dcim.module.query.application;

import net.vivans.dcim.module.device.api.dto.DevicePageOptionsResponse;
import net.vivans.dcim.module.device.application.DevicePageOptionsQueryService;
import net.vivans.dcim.module.query.api.dto.AnalysisDeviceResponse;
import net.vivans.dcim.module.query.api.dto.AnalysisMeasurementsRequest;
import net.vivans.dcim.module.query.domain.PointQuery;
import net.vivans.dcim.module.query.domain.SeriesPoint;
import net.vivans.dcim.module.query.domain.StatsSeriesPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisQueryServiceTest {

    @Mock DevicePageOptionsQueryService pageOptions;
    @Mock PointQuery pointQuery;
    @InjectMocks AnalysisQueryService service;

    @Test
    void returnsSelectedRawPointWithSamePointAndDisplayName() {
        when(pageOptions.getOptions("ANALYSIS")).thenReturn(options());
        when(pointQuery.findRawSeries(anyList(), anyList(), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(
                        new SeriesPoint(17, "AMP", 0.21, Instant.parse("2026-10-01T00:00:00Z")),
                        new SeriesPoint(17, "NOT_SELECTED", 99.0, Instant.parse("2026-10-01T00:00:00Z"))));

        var response = service.getMeasurements(request("2026-10-01", "2026-10-07", "AMP"));

        assertThat(response).hasSize(1);
        var device = response.get(0);
        assertThat(device.deviceId()).isEqualTo(17);
        assertThat(device.aggregate()).isEqualTo("raw");
        var field = device.sensors().get(0).units().get(0).fields().get(0);
        assertThat(field.pointName()).isEqualTo("AMP");
        assertThat(field.displayName()).isEqualTo("AMP");
        assertThat(field.raws()).containsExactly(
                new AnalysisDeviceResponse.RawValue(Instant.parse("2026-10-01T00:00:00Z"), 0.21));
        assertThat(field.stats()).isNull();
    }

    @Test
    void returnsMinMaxAverageForLongerRange() {
        when(pageOptions.getOptions("ANALYSIS")).thenReturn(options());
        when(pointQuery.findStatsSeries(anyList(), anyList(), any(Instant.class), any(Instant.class), eq("30m")))
                .thenReturn(List.of(new StatsSeriesPoint(17, "AMP", 0.20, 0.22, 0.21,
                        Instant.parse("2026-09-30T15:30:00Z"))));

        var response = service.getMeasurements(request("2026-09-23", "2026-10-06", "AMP"));

        var device = response.get(0);
        assertThat(device.aggregate()).isEqualTo("30m");
        var field = device.sensors().get(0).units().get(0).fields().get(0);
        assertThat(field.pointName()).isEqualTo("AMP");
        assertThat(field.displayName()).isEqualTo("AMP");
        assertThat(field.raws()).isNull();
        assertThat(field.stats()).containsExactly(new AnalysisDeviceResponse.StatsValue(
                Instant.parse("2026-09-30T15:30:00Z"), 0.20, 0.22, 0.21));
        verify(pointQuery).findStatsSeries(anyList(), anyList(), any(Instant.class), any(Instant.class), eq("30m"));
    }

    @Test
    void rejectsDeviceOutsideAnalysisPageOptions() {
        when(pageOptions.getOptions("ANALYSIS")).thenReturn(options());

        assertThatThrownBy(() -> service.getMeasurements(request("2026-10-01", "2026-10-01", "AMP", 999)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not registered for ANALYSIS");
    }

    private static DevicePageOptionsResponse options() {
        return new DevicePageOptionsResponse("ANALYSIS", "Analysis", List.of(
                new DevicePageOptionsResponse.DeviceOption(17, "RDC-PDU-1", "pdu", 4, "PDU Model",
                        "RACK-01", "Rack 01", true, List.of(
                        new DevicePageOptionsResponse.PointOption(9, "snmp", 1, 4, "AMP", "A", "CURRENT",
                                1, "CURRENT", 81, "POWER", "전력", false, true, "MODEL_POINT", 0),
                        new DevicePageOptionsResponse.PointOption(9, "snmp", 2, 4, "NOT_SELECTED", "A", "CURRENT",
                                1, "CURRENT", 81, "POWER", "전력", false, true, "MODEL_POINT", 1)
                ))));
    }

    private static AnalysisMeasurementsRequest request(String start, String end, String pointName) {
        return request(start, end, pointName, 17);
    }

    private static AnalysisMeasurementsRequest request(String start, String end, String pointName, int deviceId) {
        return new AnalysisMeasurementsRequest(LocalDate.parse(start), LocalDate.parse(end),
                List.of(new AnalysisMeasurementsRequest.Target(deviceId, List.of(pointName))));
    }
}
