package net.vivans.dcim.module.location.api.dto;

public record LocationNodeDeleteResponse(
        String deletedCode,
        int reassignedDeviceCount
) {
}
