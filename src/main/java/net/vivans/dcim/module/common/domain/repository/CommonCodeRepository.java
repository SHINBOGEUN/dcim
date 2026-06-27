package net.vivans.dcim.module.common.domain.repository;

import net.vivans.dcim.module.common.domain.model.CommonCode;

public interface CommonCodeRepository {

    CommonCode save(CommonCode code);

    boolean existsByCodeGroupIdAndCode(Integer groupId, String code);
}
