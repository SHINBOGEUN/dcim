package net.vivans.dcim.module.common.application;

import jakarta.persistence.EntityNotFoundException;
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

    @Transactional
    public CodeGroupResponse createCodeGroup(CodeGroupRequest request) {
        CodeGroup group = CodeGroup.creatCodeGroup(request.groupKey(), request.groupName());
        codeGroupRepository.save(group);
        return CodeGroupResponse.from(group);
    }

    @Transactional
    public CodeGroupResponse updateCodeGroup(Integer id, CodeGroupRequest request) {
        CodeGroup codeGroup = codeGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CodeGroup not found: " + id));
        boolean existedKey =  codeGroupRepository.existsByGroupKeyAndIdNot(request.groupKey(), codeGroup.getId());
        boolean existedName = codeGroupRepository.existsByGroupNameAndIdNot(request.groupName(), codeGroup.getId());
        if (existedKey ){
            throw new IllegalArgumentException("GroupKey already exists");
        } else if (existedName) {
            throw new IllegalArgumentException("GroupName already exists");
        }
        codeGroup.update(request.groupKey(), request.groupName());
        return CodeGroupResponse.from(codeGroupRepository.save(codeGroup));
    }

    public List<CodeGroupResponse> findAll() {
        return codeGroupRepository.findAll().stream().map(CodeGroupResponse::from).toList();
    }
}
