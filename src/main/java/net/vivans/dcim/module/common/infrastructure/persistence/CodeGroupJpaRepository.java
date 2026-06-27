package net.vivans.dcim.module.common.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.repository.CodeGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CodeGroupJpaRepository implements CodeGroupRepository {

    private final CodeGroupSpringDataRepository springDataRepository;

    @Override
    public List<CodeGroup> findAll() {
        return springDataRepository.findAll();
    }
}
