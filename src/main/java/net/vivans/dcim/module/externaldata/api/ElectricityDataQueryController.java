package net.vivans.dcim.module.externaldata.api;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.externaldata.api.dto.ElectricityDataResponses;
import net.vivans.dcim.module.externaldata.application.ElectricityDataQueryService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ElectricityDataQueryController {

    private final ElectricityDataQueryService service;

    @GetMapping("/electricity-bill/monthly")
    public ResponseEntity<ApiResponse<List<ElectricityDataResponses.MonthlyBillResponse>>> getMonthlyList() {
        return ResponseEntity.ok(ApiResponse.ok(service.getMonthlyList()));
    }

    @GetMapping("/electricity-bill/monthly/chart")
    public ResponseEntity<ApiResponse<ElectricityDataResponses.MonthlyBillChartResponse>> getMonthlyChart() {
        return ResponseEntity.ok(ApiResponse.ok(service.getMonthlyChart()));
    }

    @GetMapping("/electricity-bill/hourly/latest")
    public ResponseEntity<ApiResponse<ElectricityDataResponses.HourlyBillResponse>> getHourlyLatest() {
        return ResponseEntity.ok(ApiResponse.ok(service.getHourlyLatest()));
    }

    @GetMapping("/power-usage/daily/yesterday")
    public ResponseEntity<ApiResponse<ElectricityDataResponses.DailyUsageResponse>> getYesterday() {
        return ResponseEntity.ok(ApiResponse.ok(service.getYesterday()));
    }

    @GetMapping("/power-usage/daily/chart")
    public ResponseEntity<ApiResponse<ElectricityDataResponses.DailyUsageChartResponse>> getDailyChart(
            @RequestParam(required = false) String month) {
        YearMonth targetMonth = month == null || month.isBlank() ? YearMonth.now() : parseMonth(month);
        return ResponseEntity.ok(ApiResponse.ok(service.getDailyChart(targetMonth)));
    }

    private static YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException("month 형식이 올바르지 않습니다 (yyyy-MM): " + month);
        }
    }
}
