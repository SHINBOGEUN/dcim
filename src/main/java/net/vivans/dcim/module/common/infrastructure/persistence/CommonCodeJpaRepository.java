package net.vivans.dcim.module.common.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.common.domain.repository.CommonCodeRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommonCodeJpaRepository implements CommonCodeRepository {

    private final CommonCodeSpringDataRepository springDataRepository;

    @Override
    public CommonCode save(CommonCode code) {
        return springDataRepository.save(code);
    }

    @Override
    public boolean existsByCodeGroupIdAndCode(Integer groupId, String code) {
        return springDataRepository.existsByCodeGroupIdAndCode(groupId, code);
    }
}
