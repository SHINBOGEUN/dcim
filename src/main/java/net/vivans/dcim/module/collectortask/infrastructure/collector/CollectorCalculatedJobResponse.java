package net.vivans.dcim.module.collectortask.infrastructure.collector;

import java.time.Instant;

public record CollectorCalculatedJobResponse(
        Integer definitionId,
        Integer configVersion,
        boolean running,
        Instant lastSuccessAt,
        Instant lastFailureAt,
        int consecutiveFailureCount,
        String lastFailureReason
) {
}
