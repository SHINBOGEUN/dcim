package net.vivans.dcim.module.common.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.common.domain.repository.CommonCodeRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnitCodeResolver {
    private static final String UNIT_GROUP_KEY = "UNIT";

    private final CommonCodeRepository commonCodeRepository;

    public CommonCode resolve(Integer unitCodeId) {
        if (unitCodeId == null) {
            return null;
        }
        CommonCode code = commonCodeRepository.findById(unitCodeId)
                .orElseThrow(() -> new EntityNotFoundException("CommonCode not found: " + unitCodeId));
        if (!UNIT_GROUP_KEY.equals(code.getCodeGroup().getGroupKey())) {
            throw new IllegalArgumentException("unitCodeId must belong to UNIT");
        }
        return code;
    }
}
