package net.vivans.dcim.module.collectortask.api.dto;

import jakarta.validation.constraints.NotNull;

public record CollectionTaskGroupDeviceAddRequest(@NotNull Integer deviceId) {
}
