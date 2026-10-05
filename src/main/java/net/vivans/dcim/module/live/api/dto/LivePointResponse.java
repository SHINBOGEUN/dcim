package net.vivans.dcim.module.live.api.dto;

public record LivePointResponse(
        String name,
        String unit,
        String protocol,
        Integer sourceDeviceId,
        String sourceDeviceName
) {
    public LivePointResponse(String name, String unit) {
        this(name, unit, "snmp", null, null);
    }
}
