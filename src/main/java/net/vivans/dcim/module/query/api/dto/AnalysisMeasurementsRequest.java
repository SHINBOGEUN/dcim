package net.vivans.dcim.module.query.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record AnalysisMeasurementsRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotEmpty List<@Valid Target> targets
) {
    public record Target(
            @NotNull Integer deviceId,
            @NotEmpty List<@NotBlank String> pointNames
    ) {
    }
}
