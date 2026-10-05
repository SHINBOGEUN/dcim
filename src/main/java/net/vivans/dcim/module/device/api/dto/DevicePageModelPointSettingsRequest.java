package net.vivans.dcim.module.device.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record DevicePageModelPointSettingsRequest(
        @NotNull List<@NotNull @Valid PointSetting> points
) {
    public record PointSetting(
            @NotNull @Positive Integer protocolTypeId,
            @NotNull @Positive Integer pointId,
            @NotNull Boolean visible,
            @PositiveOrZero Integer sortOrder
    ) {
    }
}
