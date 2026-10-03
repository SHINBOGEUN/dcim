package net.vivans.dcim.module.device.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record DevicePageDevicesRequest(
        @NotNull List<@NotNull @Positive Integer> deviceIds
) {
}
