package net.vivans.dcim.module.externaldata.api.dto;

import java.util.List;

public final class ElectricityDataResponses {
    private ElectricityDataResponses() {
    }

    public record MonthlyBillResponse(
            String yearMonth,
            Double contractPower,
            Double appliedPower,
            Double usageKwh,
            Integer usageDays,
            Double laggingPowerFactor,
            Double leadingPowerFactor,
            Long billingAmount
    ) {
    }

    public record MonthlyBillChartItem(String yearMonth, Long billingAmount, Double usageKwh) {
    }

    public record MonthlyBillChartResponse(List<MonthlyBillChartItem> items) {
    }

    public record HourlyBillResponse(String usageDateTime, Long billingAmount) {
    }

    public record DailyUsageResponse(
            String usageDate,
            Double usageKwh,
            Double prevMonthSameDayKwh,
            Double prevYearSameDayKwh
    ) {
    }

    public record DailyUsageChartResponse(
            List<DailyUsageResponse> items,
            Double maxKwh,
            Double minKwh,
            Double avgKwh,
            Double totalKwh
    ) {
    }
}
