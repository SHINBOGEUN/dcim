package net.vivans.dcim.module.externaldata.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.vivans.dcim.module.externaldata.domain.model.ExternalData;
import net.vivans.dcim.module.externaldata.infrastructure.persistence.ExternalDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ElectricityDataQueryServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ExternalDataRepository repository;
    private ElectricityDataQueryService service;

    @BeforeEach
    void setUp() {
        repository = mock(ExternalDataRepository.class);
        service = new ElectricityDataQueryService(repository, objectMapper);
    }

    @Test
    void monthlyQueriesReturnOnlyLatestReceivePerBusinessMonth() throws Exception {
        ExternalData latest = record("BILLING", "MONTHLY", monthlyPayload("2026-09", 200L, 20d), "2026-10-02T00:00:00Z");
        ExternalData old = record("BILLING", "MONTHLY", monthlyPayload("2026-09", 100L, 10d), "2026-10-01T00:00:00Z");
        ExternalData previousMonth = record("BILLING", "MONTHLY", monthlyPayload("2026-08", 80L, 8d), "2026-09-02T00:00:00Z");
        when(repository.findByDataCategoryAndPeriodTypeOrderByCreateDtDescIdDesc("BILLING", "MONTHLY"))
                .thenReturn(List.of(latest, old, previousMonth));

        var rows = service.getMonthlyList();

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).yearMonth()).isEqualTo("2026-09");
        assertThat(rows.get(0).billingAmount()).isEqualTo(200L);
        assertThat(rows.get(0).usageKwh()).isEqualTo(20d);
        assertThat(service.getMonthlyChart().items()).hasSize(2);
    }

    @Test
    void electricityQueriesIgnoreOtherResourceTypesInTheSameCategory() throws Exception {
        var gas = objectMapper.createObjectNode().put("resourceType", "GAS").put("yearMonth", "2026-09");
        gas.putObject("metrics").set("billingAmount", metric(500L, "KRW"));
        ExternalData gasRecord = record("BILLING", "MONTHLY", gas, "2026-10-03T00:00:00Z");
        ExternalData electricityRecord = record("BILLING", "MONTHLY", monthlyPayload("2026-09", 200L, 20d),
                "2026-10-02T00:00:00Z");
        when(repository.findByDataCategoryAndPeriodTypeOrderByCreateDtDescIdDesc("BILLING", "MONTHLY"))
                .thenReturn(List.of(gasRecord, electricityRecord));

        var rows = service.getMonthlyList();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).billingAmount()).isEqualTo(200L);
    }

    @Test
    void dailyChartDoesNotDoubleCountRetransmittedBusinessDate() throws Exception {
        ExternalData latest = record("CONSUMPTION", "DAILY", dailyPayload("2026-09-01", 150d), "2026-09-03T00:00:00Z");
        ExternalData old = record("CONSUMPTION", "DAILY", dailyPayload("2026-09-01", 100d), "2026-09-02T00:00:00Z");
        ExternalData secondDay = record("CONSUMPTION", "DAILY", dailyPayload("2026-09-02", 250d), "2026-09-03T01:00:00Z");
        when(repository.findByDataCategoryAndPeriodTypeOrderByCreateDtDescIdDesc("CONSUMPTION", "DAILY"))
                .thenReturn(List.of(secondDay, latest, old));

        var chart = service.getDailyChart(java.time.YearMonth.of(2026, 9));

        assertThat(chart.items()).hasSize(2);
        assertThat(chart.items().get(0).usageKwh()).isEqualTo(150d);
        assertThat(chart.totalKwh()).isEqualTo(400d);
        assertThat(chart.avgKwh()).isEqualTo(200d);
    }

    @Test
    void yesterdayQueryReturnsLatestReceivedRecordForYesterday() throws Exception {
        String yesterday = LocalDate.now().minusDays(1).toString();
        ExternalData latest = record("CONSUMPTION", "DAILY", dailyPayload(yesterday, 150d), "2026-09-03T00:00:00Z");
        ExternalData old = record("CONSUMPTION", "DAILY", dailyPayload(yesterday, 100d), "2026-09-02T00:00:00Z");
        when(repository.findByDataCategoryAndPeriodTypeOrderByCreateDtDescIdDesc("CONSUMPTION", "DAILY"))
                .thenReturn(List.of(latest, old));

        var response = service.getYesterday();

        assertThat(response.usageDate()).isEqualTo(yesterday);
        assertThat(response.usageKwh()).isEqualTo(150d);
    }

    @Test
    void hourlyLatestDeduplicatesByMinuteAndUsesLatestBusinessTime() throws Exception {
        ExternalData olderMinuteReceivedLast = record("BILLING", "HOURLY", hourlyPayload("2026-09-18T14:10:23", 52340L), "2026-09-18T14:14:00Z");
        ExternalData retransmitted = record("BILLING", "HOURLY", hourlyPayload("2026-09-18T14:11:45", 55000L), "2026-09-18T14:13:00Z");
        ExternalData original = record("BILLING", "HOURLY", hourlyPayload("2026-09-18T14:11:01", 54000L), "2026-09-18T14:12:00Z");
        when(repository.findByDataCategoryAndPeriodTypeOrderByCreateDtDescIdDesc("BILLING", "HOURLY"))
                .thenReturn(List.of(olderMinuteReceivedLast, retransmitted, original));

        assertThat(service.getHourlyLatest().usageDateTime()).isEqualTo("2026-09-18T14:11:00");
        assertThat(service.getHourlyLatest().billingAmount()).isEqualTo(55000L);
    }

    private ExternalData record(String category, String period, JsonNode payload, String receivedAt) throws Exception {
        return ExternalData.receive(category, period, objectMapper.writeValueAsString(payload), Instant.parse(receivedAt));
    }

    private JsonNode monthlyPayload(String month, long amount, double usage) {
        var payload = objectMapper.createObjectNode().put("resourceType", "ELECTRICITY").put("yearMonth", month);
        var metrics = payload.putObject("metrics");
        metrics.set("billingAmount", metric(amount, "KRW"));
        metrics.set("usageKwh", metric(usage, "kWh"));
        return payload;
    }

    private JsonNode dailyPayload(String date, double usage) {
        var payload = objectMapper.createObjectNode().put("resourceType", "ELECTRICITY").put("usageDate", date);
        payload.putObject("metrics").set("usageKwh", metric(usage, "kWh"));
        return payload;
    }

    private JsonNode hourlyPayload(String dateTime, long amount) {
        var payload = objectMapper.createObjectNode().put("resourceType", "ELECTRICITY").put("usageDateTime", dateTime);
        payload.putObject("metrics").set("billingAmount", metric(amount, "KRW"));
        return payload;
    }

    private JsonNode metric(Number value, String unit) {
        var metric = objectMapper.createObjectNode();
        metric.set("value", objectMapper.valueToTree(value));
        metric.put("unit", unit);
        return metric;
    }
}
