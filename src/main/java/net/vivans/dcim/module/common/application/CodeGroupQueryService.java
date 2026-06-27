package net.vivans.dcim.module.common.application;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.api.dto.CodeGroupResponse;
import net.vivans.dcim.module.common.domain.repository.CodeGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeGroupQueryService {

    private final CodeGroupRepository codeGroupRepository;

    public List<CodeGroupResponse> findAll() {
        return codeGroupRepository.findAll().stream()
                .map(CodeGroupResponse::from)
                .toList();
    }
}
