package net.vivans.dcim.module.externaldata.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ExternalDataSaveRequest(
        @NotBlank
        @Pattern(regexp = "[A-Za-z][A-Za-z0-9_-]{0,49}")
        String dataCategory,
        @NotBlank
        String periodType,
        @NotNull
        JsonNode payload
) {
}
