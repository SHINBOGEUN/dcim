package net.vivans.dcim.module.pue.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CalculatedMetricRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 500) String formula,
        @Size(max = 32) String resultUnit,
        String calculationCron,
        Boolean collectionEnabled,
        @NotEmpty @Size(max = 32) List<@Valid Source> sources) {
    public record Source(@NotNull @Positive Integer deviceId,
                         @NotBlank @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]{0,31}") String alias,
                         @NotBlank @Size(max = 100) String pointName,
                         @NotBlank @Pattern(regexp = "(?i)snmp|modbus") String protocol) { }
}
