package net.vivans.dcim.module.device.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

/** LSB를 0번으로 세며, 매핑하지 않은 비트값은 원시 추출값 또는 unmappedValue로 저장한다. */
public record DeviceModbusBitFieldRequest(
        @NotBlank @Size(max = 255) String pointName,
        @NotNull @Min(0) @Max(31) Integer bitOffset,
        @NotNull @Min(1) @Max(32) Integer bitWidth,
        Map<String, Long> valueMap,
        Long unmappedValue
) {
}
