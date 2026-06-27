package net.vivans.dcim.module.common.application;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.api.dto.CodeGroupRequest;
import net.vivans.dcim.module.common.api.dto.CodeGroupResponse;
import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.repository.CodeGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeGroupQueryService {

    private final CodeGroupRepository codeGroupRepository;

    public CodeGroupResponse createCodeGroup(CodeGroupRequest request){
        CodeGroup group = new CodeGroup(request.groupKey(), request.groupName());
        codeGroupRepository.save(group);
        return CodeGroupResponse.from(group);
    }

    public List<CodeGroupResponse> findAll() {
        return codeGroupRepository.findAll().stream()
                .map(CodeGroupResponse::from)
                .toList();
    }
}
