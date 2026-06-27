package net.vivans.dcim.module.common.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.api.dto.CommonCodeRequest;
import net.vivans.dcim.module.common.api.dto.CommonCodeResponse;
import net.vivans.dcim.module.common.application.CommonCodeQueryService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/common-codes")
@RequiredArgsConstructor
@Tag(name = "common-codes", description = "공통 코드 관련 API")
public class CommonCodeController {

    private final CommonCodeQueryService commonCodeQueryService;

    @PostMapping
    @Operation(summary = "공통 코드 등록 API")
    public ResponseEntity<ApiResponse<CommonCodeResponse>> createCommonCode(
            @Valid @RequestBody CommonCodeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(commonCodeQueryService.createCommonCode(request)));
    }
}
