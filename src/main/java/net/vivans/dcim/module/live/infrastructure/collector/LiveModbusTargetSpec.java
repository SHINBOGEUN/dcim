package net.vivans.dcim.module.live.infrastructure.collector;

import net.vivans.dcim.module.collectortask.application.CollectionGroupModbusSpec;

import java.util.Map;

/** Modbus live target retains the regular collection target's read/bit-field contract. */
public record LiveModbusTargetSpec(
        String deviceName,
        Integer sourceDeviceId,
        CollectionGroupModbusSpec.ModbusTarget target,
        Map<String, String> units
) {
}
