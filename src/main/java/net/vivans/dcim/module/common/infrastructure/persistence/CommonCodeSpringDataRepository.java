package net.vivans.dcim.module.common.infrastructure.persistence;

import net.vivans.dcim.module.common.domain.model.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonCodeSpringDataRepository extends JpaRepository<CommonCode, Integer> {

    boolean existsByCodeGroupIdAndCode(Integer groupId, String code);
    boolean existsByCodeAndIdNot(String code, Integer id);
    boolean existsByNameAndIdNot(String name, Integer id);
}
