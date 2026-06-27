package net.vivans.dcim.module.common.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.api.dto.CommonCodeRequest;
import net.vivans.dcim.module.common.api.dto.CommonCodeResponse;
import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.common.domain.repository.CodeGroupRepository;
import net.vivans.dcim.module.common.domain.repository.CommonCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeQueryService {

    private final CommonCodeRepository commonCodeRepository;
    private final CodeGroupRepository codeGroupRepository;

    @Transactional
    public CommonCodeResponse createCommonCode(CommonCodeRequest request) {
        CodeGroup codeGroup = codeGroupRepository.findById(request.groupId())
                .orElseThrow(() -> new EntityNotFoundException("CodeGroup not found: " + request.groupId()));

        if (commonCodeRepository.existsByCodeGroupIdAndCode(codeGroup.getId(), request.code())) {
            throw new IllegalArgumentException("Code already exists in this group");
        }

        CommonCode code = CommonCode.createCommonCode(
                codeGroup, request.code(), request.name(), request.sortOrder());
        commonCodeRepository.save(code);
        return CommonCodeResponse.from(code);
    }
}
