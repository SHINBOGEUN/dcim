package net.vivans.dcim.module.externaldata.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public record ExternalDataResponse(
        Long id,
        String dataCategory,
        String periodType,
        JsonNode payload,
        Instant createDt
) {
}
