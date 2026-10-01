package net.vivans.dcim.module.device.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DeviceModbusBitFieldsReplaceRequest(@NotNull List<@Valid DeviceModbusBitFieldRequest> fields) {
}
