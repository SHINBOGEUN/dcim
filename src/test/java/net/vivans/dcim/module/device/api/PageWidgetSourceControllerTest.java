package net.vivans.dcim.module.device.api;

import net.vivans.dcim.module.device.application.DeviceMeasurementSourceCatalog;
import net.vivans.dcim.module.query.domain.LastPoint;
import net.vivans.dcim.module.query.domain.PointQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageWidgetSourceControllerTest {
    @Mock DeviceMeasurementSourceCatalog sourceCatalog;
    @Mock PointQuery pointQuery;
    @InjectMocks PageWidgetSourceController controller;

    @Test
    void previewReadsLatestValueForSelectedModbusDestination() {
        DeviceMeasurementSourceCatalog.Source source = new DeviceMeasurementSourceCatalog.Source(
                31, "CHILLER", 10, "modbus", "TOTAL_WT", "W", "POWER", 17, "READING", false);
        LastPoint last = new LastPoint(31, "TOTAL_WT", 1200, Instant.parse("2026-10-02T00:00:00Z"));
        when(sourceCatalog.availableSources(java.util.Set.of(31))).thenReturn(List.of(source));
        when(pointQuery.findLast(List.of(31), List.of("TOTAL_WT"), Duration.ofHours(24), "modbus"))
                .thenReturn(List.of(last));

        assertThat(controller.preview(31, "TOTAL_WT", "modbus").getData()).isEqualTo(last);
        verify(pointQuery).findLast(List.of(31), List.of("TOTAL_WT"), Duration.ofHours(24), "modbus");
    }

    @Test
    void ambiguousPointCannotBePreviewedWithoutUniqueProtocolBinding() {
        DeviceMeasurementSourceCatalog.Source ambiguous = new DeviceMeasurementSourceCatalog.Source(
                31, "CHILLER", 10, "modbus", "TOTAL_WT", "W", "POWER", 17, "READING", true);
        when(sourceCatalog.availableSources(java.util.Set.of(31))).thenReturn(List.of(ambiguous));

        assertThatThrownBy(() -> controller.preview(31, "TOTAL_WT", "modbus"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(pointQuery);
    }
}
