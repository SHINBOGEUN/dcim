package net.vivans.dcim.module.device.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.device.api.dto.PageWidgetCreateRequest;
import net.vivans.dcim.module.device.api.dto.PageWidgetResponse;
import net.vivans.dcim.module.device.api.dto.PageWidgetUpdateRequest;
import net.vivans.dcim.module.device.domain.model.PageWidget;
import net.vivans.dcim.module.device.domain.model.PageWidgetChartRangePreset;
import net.vivans.dcim.module.device.domain.model.PageWidgetQueryKind;
import net.vivans.dcim.module.device.domain.repository.PageWidgetRepository;
import net.vivans.dcim.module.calculated.domain.model.CalculatedMetric;
import net.vivans.dcim.module.calculated.domain.repository.CalculatedMetricRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CalculatedMetricPageWidgetHandler {
    private final PageWidgetSpecializedSupport support;
    private final PageWidgetRepository pageWidgetRepository;
    private final CalculatedMetricRepository definitionRepository;

    @Transactional
    public PageWidgetResponse createLinked(CommonCode pageCode, PageWidgetCreateRequest request) {
        CalculatedMetric definition = findDefinition(request.pueDefinitionId());
        PageWidget widget = PageWidget.createCalculated(pageCode, request.name(),
                request.enabled() == null || request.enabled(), definition,
                PageWidgetChartRangePreset.from(request.pueRangePreset()), request.pueFreshnessMinutes());
        widget.updateDataFreshnessMinutes(request.dataFreshnessMinutes());
        support.applyLayout(widget, request.layout());
        return PageWidgetResponse.from(pageWidgetRepository.save(widget));
    }

    @Transactional
    public PageWidgetResponse updateLinked(PageWidget widget, PageWidgetUpdateRequest request) {
        if (widget.getQueryKind() != PageWidgetQueryKind.calculated) {
            throw new IllegalArgumentException("widget type cannot be changed to calculated");
        }
        CalculatedMetric definition = findDefinition(request.pueDefinitionId());
        widget.updateCalculated(request.name().trim(),
                request.enabled() == null ? widget.isEnabled() : request.enabled(), definition,
                PageWidgetChartRangePreset.from(request.pueRangePreset()), request.pueFreshnessMinutes());
        widget.updateDataFreshnessMinutes(request.dataFreshnessMinutes());
        support.applyLayout(widget, request.layout());
        return PageWidgetResponse.from(pageWidgetRepository.save(widget));
    }

    private CalculatedMetric findDefinition(Integer id) {
        if (id == null) throw new IllegalArgumentException("calculated metric id is required");
        return definitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Calculated metric not found: " + id));
    }
}
