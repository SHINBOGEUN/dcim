package net.vivans.dcim.module.query.application;

import net.vivans.dcim.module.device.domain.model.PageWidget;
import net.vivans.dcim.module.device.domain.model.PageWidgetChartRangePreset;
import net.vivans.dcim.module.device.domain.model.PageWidgetCalculated;
import net.vivans.dcim.module.device.domain.model.PageWidgetQueryKind;
import net.vivans.dcim.module.device.domain.repository.PageWidgetRepository;
import net.vivans.dcim.module.calculated.domain.model.CalculatedMetric;
import net.vivans.dcim.module.query.domain.PointQuery;
import net.vivans.dcim.module.query.domain.CalculatedMetricLastPoint;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CalculatedMetricQueryServiceTest {
    private final PointQuery points = mock(PointQuery.class);
    private final PageWidgetRepository widgets = mock(PageWidgetRepository.class);
    private final CalculatedMetricQueryService service = new CalculatedMetricQueryService(points, widgets, new WidgetDataStatusResolver());

    @Test
    void returnsSavedCalculationWithItsInputs() {
        widget(42, 7, 3);
        when(points.findLastCalculated(eq(7), eq(3), any(Duration.class)))
                .thenReturn(Optional.of(new CalculatedMetricLastPoint(2.5, Instant.now(), Map.of("A", 10.0, "B", 4.0))));

        var result = service.getCalculated(42, null, null);

        assertThat(result.complete()).isTrue();
        assertThat(result.value()).isEqualByComparingTo("2.5000");
        assertThat(result.formula()).isEqualTo("A / B");
        assertThat(result.inputs()).containsEntry("A", 10.0).containsEntry("B", 4.0);
        assertThat(result.calculationStatus()).isEqualTo("OK");
        verify(points, never()).findLastCalculatedPreviousVersion(eq(7), eq(3), any(Duration.class));
    }

    @Test
    void showsPreviousResultUntilCurrentVersionIsCollected() {
        widget(42, 7, 3);
        Instant previousSavedAt = Instant.now().minusSeconds(120);
        when(points.findLastCalculated(eq(7), eq(3), any(Duration.class))).thenReturn(Optional.empty());
        when(points.findLastCalculatedPreviousVersion(eq(7), eq(3), eq(Duration.ofDays(3))))
                .thenReturn(Optional.of(new CalculatedMetricLastPoint(2.5, previousSavedAt, Map.of("A", 10.0))));

        var result = service.getCalculated(42, null, null);

        assertThat(result.value()).isEqualByComparingTo("2.5000");
        assertThat(result.calculationStatus()).isEqualTo("PREVIOUS_VERSION");
        assertThat(result.complete()).isFalse();
        assertThat(result.unit()).isNull();
        assertThat(result.inputs()).isEmpty();
        assertThat(result.dataStatus().status()).isEqualTo("PREVIOUS");
        assertThat(result.dataStatus().latestCollectedAt()).isEqualTo(previousSavedAt);
    }

    @Test
    void reportsMissingCalculationWithoutLegacyPowerFields() {
        widget(42, 7, 3);
        when(points.findLastCalculated(eq(7), eq(3), any(Duration.class))).thenReturn(Optional.empty());
        when(points.findLastCalculatedPreviousVersion(eq(7), eq(3), any(Duration.class))).thenReturn(Optional.empty());

        var result = service.getCalculated(42, null, null);

        assertThat(result.complete()).isFalse();
        assertThat(result.value()).isNull();
        assertThat(result.inputs()).isEmpty();
        assertThat(result.calculationStatus()).isEqualTo("MISSING_DATA");
    }

    @Test
    void staleCurrentVersionDoesNotFallBackToPreviousVersion() {
        widget(42, 7, 3);
        when(points.findLastCalculated(eq(7), eq(3), any(Duration.class)))
                .thenReturn(Optional.of(new CalculatedMetricLastPoint(2.5, Instant.now().minusSeconds(3600), Map.of())));

        var result = service.getCalculated(42, null, null);

        assertThat(result.value()).isNull();
        assertThat(result.calculationStatus()).isEqualTo("STALE_DATA");
        verify(points, never()).findLastCalculatedPreviousVersion(eq(7), eq(3), any(Duration.class));
    }

    private void widget(int widgetId, int metricId, int version) {
        PageWidget widget = mock(PageWidget.class);
        PageWidgetCalculated binding = mock(PageWidgetCalculated.class);
        CalculatedMetric definition = mock(CalculatedMetric.class);
        when(widgets.findById(widgetId)).thenReturn(Optional.of(widget));
        when(widget.getQueryKind()).thenReturn(PageWidgetQueryKind.calculated);
        when(widget.isEnabled()).thenReturn(true);
        when(widget.getCalculatedMetricId()).thenReturn(metricId);
        when(widget.getCalculated()).thenReturn(binding);
        when(binding.getCalculatedMetric()).thenReturn(definition);
        when(definition.getId()).thenReturn(metricId);
        when(definition.getConfigVersion()).thenReturn(version);
        when(definition.getResultUnit()).thenReturn("PUE");
        when(definition.getFormula()).thenReturn("A / B");
        when(widget.getCalculatedRangePreset()).thenReturn(PageWidgetChartRangePreset.last_24h);
        when(widget.getCalculatedFreshnessMinutes()).thenReturn(15);
    }
}
