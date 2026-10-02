package net.vivans.dcim.module.query.application;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.device.domain.model.PageWidget;
import net.vivans.dcim.module.device.domain.model.PageWidgetChartRangePreset;
import net.vivans.dcim.module.device.domain.model.PageWidgetQueryKind;
import net.vivans.dcim.module.device.domain.repository.PageWidgetRepository;
import net.vivans.dcim.module.pue.domain.model.PueDefinition;
import net.vivans.dcim.module.query.api.dto.PueQueryResponse;
import net.vivans.dcim.module.query.api.dto.PueTrendPointResponse;
import net.vivans.dcim.module.query.api.dto.WidgetDataStatusResponse;
import net.vivans.dcim.module.query.domain.PointQuery;
import net.vivans.dcim.module.query.domain.PueLastPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PueQueryService {
    private final PointQuery pointQuery;
    private final PageWidgetRepository pageWidgetRepository;
    private final WidgetDataStatusResolver widgetDataStatusResolver;

    public PueQueryResponse getPue(Integer widgetId, String rangePresetOverride, String windowOverride) {
        PageWidget widget = PageWidgetFinder.findRequired(pageWidgetRepository, widgetId);
        if (widget.getQueryKind() != PageWidgetQueryKind.calculated) {
            throw new IllegalArgumentException("queryKind must be calculated");
        }
        if (!widget.isEnabled()) throw new IllegalArgumentException("widget is disabled");
        if (widget.getPueDefinitionId() == null) throw new IllegalArgumentException("calculated widget has no metric");

        PueDefinition definition = widget.getPue().getPueDefinition();
        PageWidgetChartRangePreset preset = rangePresetOverride == null || rangePresetOverride.isBlank()
                ? widget.getPueRangePreset()
                : PageWidgetChartRangePreset.from(rangePresetOverride);
        if (preset == null) preset = PageWidgetChartRangePreset.last_24h;
        QueryRanges.Range range = QueryRanges.resolve(preset);
        PueLastPoint point = pointQuery.findLastCalculated(definition.getId(), definition.getConfigVersion(),
                Duration.between(range.start(), range.end())).orElse(null);
        boolean stale = point != null && widget.getPueFreshnessMinutes() != null
                && point.time().isBefore(Instant.now().minusSeconds(widget.getPueFreshnessMinutes().longValue() * 60));
        boolean complete = point != null && !stale;
        WidgetDataStatusResponse status = widgetDataStatusResolver.resolve(
                Collections.singletonList(point == null ? null : point.time()), widget.getPueFreshnessMinutes());
        List<PueTrendPointResponse> trend = List.of();
        if ((rangePresetOverride != null && !rangePresetOverride.isBlank())
                || (windowOverride != null && !windowOverride.isBlank())) {
            String window = windowOverride == null || windowOverride.isBlank() ? "15m" : windowOverride.trim();
            if (!java.util.Set.of("1m", "5m", "15m", "1h", "1d").contains(window)) {
                throw new IllegalArgumentException("window must be 1m, 5m, 15m, 1h, or 1d");
            }
            trend = pointQuery.findPueSeries(definition.getId(), range.start(), range.end(), window)
                    .stream().map(item -> new PueTrendPointResponse(item.time(), QueryValues.round4(item.value())))
                    .toList();
        }
        return new PueQueryResponse(complete ? QueryValues.round4(point.value()) : null,
                definition.getResultUnit(), preset.name(), range.start(), range.end(), complete,
                point == null ? "MISSING_DATA" : stale ? "STALE_DATA" : "OK",
                trend, status, definition.getFormula(),
                complete ? point.inputs() : Map.of());
    }
}
