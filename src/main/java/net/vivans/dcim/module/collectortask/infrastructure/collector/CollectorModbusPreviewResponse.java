package net.vivans.dcim.module.collectortask.infrastructure.collector;

import java.util.List;
import java.util.Map;

public record CollectorModbusPreviewResponse(List<TargetResult> targets) {
    public record TargetResult(Integer deviceId, String host, int port, int unitId,
                               Map<String, Object> values, String error, long elapsedMs) {
    }
}
