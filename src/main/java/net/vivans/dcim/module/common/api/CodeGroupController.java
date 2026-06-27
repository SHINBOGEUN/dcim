package net.vivans.dcim.module.common.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.api.dto.CodeGroupResponse;
import net.vivans.dcim.module.common.application.CodeGroupQueryService;
import net.vivans.dcim.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/code-groups")
@Tag(name = "code-groups", description = "코드 그룹 관련 API")
public class CodeGroupController {

    private final CodeGroupQueryService codeGroupQueryService;

    @Operation(summary = "코드 그룹 전체 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CodeGroupResponse>>> getCodeGroups() {
        return ResponseEntity.ok(ApiResponse.ok(codeGroupQueryService.findAll()));
    }
}
