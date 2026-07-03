package net.vivans.dcim.module.devicemodel.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/device-models")
@Tag(name = "device-model", description = "장비 제품 모델 CRUD API")
public class DeviceModelController {
}
