package net.vivans.dcim.module.devicemodel.api.dto;

import jakarta.validation.constraints.NotBlank;

/** 제조사와 설명을 생략하면 원본 값을 사용한다. */
public record DeviceModelCloneRequest(@NotBlank String name, String manufacturer, String description) {
}
