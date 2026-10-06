package net.vivans.dcim.module.externaldata.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.externaldata.api.dto.ElectricityDataResponses;
import net.vivans.dcim.module.externaldata.domain.model.ExternalData;
import net.vivans.dcim.module.externaldata.infrastructure.persistence.ExternalDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ElectricityDataQueryService {

    private static final String ELECTRICITY = "ELECTRICITY";
    private static final DateTimeFormatter MINUTE_KEY = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:00");

    private final ExternalDataRepository repository;
    private final ObjectMapper objectMapper;

    public List<ElectricityDataResponses.MonthlyBillResponse> getMonthlyList() {
        return latestByBusinessKey("BILLING", "MONTHLY", "yearMonth").stream()
                .sorted(Comparator.comparing(item -> text(item.payload(), "yearMonth"), Comparator.reverseOrder()))
                .map(item -> toMonthlyResponse(text(item.payload(), "yearMonth"), item.payload()))
                .toList();
    }

    public ElectricityDataResponses.MonthlyBillChartResponse getMonthlyChart() {
        List<ElectricityDataResponses.MonthlyBillResponse> recent = getMonthlyList().stream().limit(12)
                .sorted(Comparator.comparing(ElectricityDataResponses.MonthlyBillResponse::yearMonth))
                .toList();
        return new ElectricityDataResponses.MonthlyBillChartResponse(recent.stream()
                .map(item -> new ElectricityDataResponses.MonthlyBillChartItem(
                        item.yearMonth(), item.billingAmount(), item.usageKwh()))
                .toList());
    }

    public ElectricityDataResponses.HourlyBillResponse getHourlyLatest() {
        return latestByBusinessKey("BILLING", "HOURLY", "usageDateTime").stream()
                .max(Comparator.comparing(item -> businessKey(item.payload(), "usageDateTime")))
                .map(item -> new ElectricityDataResponses.HourlyBillResponse(
                        businessKey(item.payload(), "usageDateTime"), longMetric(item.payload(), "billingAmount")))
                .orElseThrow(() -> new IllegalArgumentException("저장된 시간별 전기요금 데이터가 없습니다"));
    }

    public ElectricityDataResponses.DailyUsageResponse getYesterday() {
        String yesterday = LocalDate.now().minusDays(1).toString();
        return latestByBusinessKey("CONSUMPTION", "DAILY", "usageDate").stream()
                .filter(item -> yesterday.equals(text(item.payload(), "usageDate")))
                .findFirst()
                .map(item -> toDailyResponse(yesterday, item.payload()))
                .orElseThrow(() -> new IllegalArgumentException("전일(" + yesterday + ") 사용량 데이터가 없습니다"));
    }

    public ElectricityDataResponses.DailyUsageChartResponse getDailyChart(YearMonth month) {
        String firstDay = month.atDay(1).toString();
        String lastDay = month.atEndOfMonth().toString();
        List<ElectricityDataResponses.DailyUsageResponse> items = latestByBusinessKey("CONSUMPTION", "DAILY", "usageDate").stream()
                .filter(item -> {
                    String date = text(item.payload(), "usageDate");
                    return date.compareTo(firstDay) >= 0 && date.compareTo(lastDay) <= 0;
                })
                .sorted(Comparator.comparing(item -> text(item.payload(), "usageDate")))
                .map(item -> toDailyResponse(text(item.payload(), "usageDate"), item.payload()))
                .toList();
        DoubleSummaryStatistics stats = items.stream().map(ElectricityDataResponses.DailyUsageResponse::usageKwh)
                .filter(Objects::nonNull).mapToDouble(Double::doubleValue).summaryStatistics();
        boolean hasData = stats.getCount() > 0;
        return new ElectricityDataResponses.DailyUsageChartResponse(items,
                hasData ? stats.getMax() : null,
                hasData ? stats.getMin() : null,
                hasData ? stats.getAverage() : null,
                hasData ? stats.getSum() : null);
    }

    private List<PayloadEntry> latestByBusinessKey(String category, String period, String businessKey) {
        Map<String, PayloadEntry> latest = new LinkedHashMap<>();
        for (ExternalData record : repository.findByDataCategoryAndPeriodTypeOrderByCreateDtDescIdDesc(category, period)) {
            JsonNode payload = readPayload(record);
            if (!ELECTRICITY.equals(text(payload, "resourceType"))) {
                continue;
            }
            String key = businessKey(payload, businessKey);
            if (key != null) {
                latest.putIfAbsent(key, new PayloadEntry(payload));
            }
        }
        return List.copyOf(latest.values());
    }

    private static String businessKey(JsonNode payload, String field) {
        String value = text(payload, field);
        if (value == null || value.isBlank()) {
            return null;
        }
        if (!"usageDateTime".equals(field)) {
            return value;
        }
        try {
            return LocalDateTime.parse(value).withSecond(0).withNano(0).format(MINUTE_KEY);
        } catch (DateTimeException exception) {
            return null;
        }
    }

    private ElectricityDataResponses.MonthlyBillResponse toMonthlyResponse(String yearMonth, JsonNode payload) {
        return new ElectricityDataResponses.MonthlyBillResponse(yearMonth,
                doubleMetric(payload, "contractPower"), doubleMetric(payload, "appliedPower"),
                doubleMetric(payload, "usageKwh"), integerMetric(payload, "usageDays"),
                doubleMetric(payload, "laggingPowerFactor"), doubleMetric(payload, "leadingPowerFactor"),
                longMetric(payload, "billingAmount"));
    }

    private ElectricityDataResponses.DailyUsageResponse toDailyResponse(String usageDate, JsonNode payload) {
        return new ElectricityDataResponses.DailyUsageResponse(usageDate,
                doubleMetric(payload, "usageKwh"), doubleMetric(payload, "prevMonthSameDayKwh"),
                doubleMetric(payload, "prevYearSameDayKwh"));
    }

    private JsonNode readPayload(ExternalData record) {
        try {
            return objectMapper.readTree(record.getPayloadJson());
        } catch (Exception exception) {
            throw new IllegalStateException("stored external data payload is not valid JSON: id=" + record.getId(), exception);
        }
    }

    private static String text(JsonNode payload, String field) {
        JsonNode value = payload.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static Double doubleMetric(JsonNode payload, String name) {
        JsonNode value = payload.path("metrics").path(name).path("value");
        return value.isNumber() ? value.doubleValue() : null;
    }

    private static Integer integerMetric(JsonNode payload, String name) {
        JsonNode value = payload.path("metrics").path(name).path("value");
        return value.isIntegralNumber() ? value.intValue() : null;
    }

    private static Long longMetric(JsonNode payload, String name) {
        JsonNode value = payload.path("metrics").path(name).path("value");
        return value.isIntegralNumber() ? value.longValue() : null;
    }

    private record PayloadEntry(JsonNode payload) {
    }
}
