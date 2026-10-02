package net.vivans.dcim.module.query.application;

import net.vivans.dcim.module.device.domain.model.PageWidget;
import net.vivans.dcim.module.device.domain.model.PageWidgetChartRangePreset;
import net.vivans.dcim.module.device.domain.model.PageWidgetPue;
import net.vivans.dcim.module.device.domain.model.PageWidgetQueryKind;
import net.vivans.dcim.module.device.domain.repository.PageWidgetRepository;
import net.vivans.dcim.module.pue.domain.model.PueDefinition;
import net.vivans.dcim.module.query.domain.PointQuery;
import net.vivans.dcim.module.query.domain.PueLastPoint;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CalculatedMetricQueryServiceTest {
    private final PointQuery points = mock(PointQuery.class);
    private final PageWidgetRepository widgets = mock(PageWidgetRepository.class);
    private final PueQueryService service = new PueQueryService(points, widgets, new WidgetDataStatusResolver());

    @Test
    void returnsSavedCalculationWithItsInputs() {
        widget(42, 7, 3);
        when(points.findLastCalculated(eq(7), eq(3), any(Duration.class)))
                .thenReturn(Optional.of(new PueLastPoint(2.5, Instant.now(), Map.of("A", 10.0, "B", 4.0))));

        var result = service.getPue(42, null, null);

        assertThat(result.complete()).isTrue();
        assertThat(result.value()).isEqualByComparingTo("2.5000");
        assertThat(result.formula()).isEqualTo("A / B");
        assertThat(result.inputs()).containsEntry("A", 10.0).containsEntry("B", 4.0);
        assertThat(result.calculationStatus()).isEqualTo("OK");
    }

    @Test
    void reportsMissingCalculationWithoutLegacyPowerFields() {
        widget(42, 7, 3);
        when(points.findLastCalculated(eq(7), eq(3), any(Duration.class))).thenReturn(Optional.empty());

        var result = service.getPue(42, null, null);

        assertThat(result.complete()).isFalse();
        assertThat(result.value()).isNull();
        assertThat(result.inputs()).isEmpty();
        assertThat(result.calculationStatus()).isEqualTo("MISSING_DATA");
    }

    private void widget(int widgetId, int metricId, int version) {
        PageWidget widget = mock(PageWidget.class);
        PageWidgetPue binding = mock(PageWidgetPue.class);
        PueDefinition definition = mock(PueDefinition.class);
        when(widgets.findById(widgetId)).thenReturn(Optional.of(widget));
        when(widget.getQueryKind()).thenReturn(PageWidgetQueryKind.calculated);
        when(widget.isEnabled()).thenReturn(true);
        when(widget.getPueDefinitionId()).thenReturn(metricId);
        when(widget.getPue()).thenReturn(binding);
        when(binding.getPueDefinition()).thenReturn(definition);
        when(definition.getId()).thenReturn(metricId);
        when(definition.getConfigVersion()).thenReturn(version);
        when(definition.getResultUnit()).thenReturn("PUE");
        when(definition.getFormula()).thenReturn("A / B");
        when(widget.getPueRangePreset()).thenReturn(PageWidgetChartRangePreset.last_24h);
        when(widget.getPueFreshnessMinutes()).thenReturn(15);
    }
}
