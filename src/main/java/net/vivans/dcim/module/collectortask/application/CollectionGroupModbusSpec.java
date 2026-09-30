package net.vivans.dcim.module.collectortask.application;

import java.util.List;

/** Collector의 ModbusCollectionGroupSpec JSON 계약과 동일한 필드만 직렬화한다. */
public record CollectionGroupModbusSpec(
        Integer taskId,
        Integer groupId,
        Integer modelId,
        String protocol,
        String cronExpression,
        int timeoutMs,
        int retries,
        int maxConcurrency,
        List<ModbusPoint> points,
        List<ModbusTarget> targets,
        List<String> skipped
) {
    public record ModbusPoint(
            String name,
            String registerType,
            Integer address,
            String dataType,
            String byteOrder,
            Double scale,
            Double offset
    ) {
    }

    public record ModbusTarget(
            Integer deviceId,
            String host,
            int port,
            Integer unitId,
            List<ModbusPoint> points
    ) {
    }
}
