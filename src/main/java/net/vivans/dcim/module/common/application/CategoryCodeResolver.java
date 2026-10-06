package net.vivans.dcim.module.common.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.common.domain.repository.CommonCodeRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryCodeResolver {
    private static final String CATEGORY_GROUP_KEY = "CATEGORY";

    private final CommonCodeRepository commonCodeRepository;

    /** 분류가 생략되면 미분류(null)로 두고, 지정된 코드는 CATEGORY 그룹인지 검증한다. */
    public CommonCode resolve(Integer categoryCodeId) {
        if (categoryCodeId == null) {
            return null;
        }
        CommonCode code = commonCodeRepository.findById(categoryCodeId)
                .orElseThrow(() -> new EntityNotFoundException("CommonCode not found: " + categoryCodeId));
        if (!CATEGORY_GROUP_KEY.equals(code.getCodeGroup().getGroupKey())) {
            throw new IllegalArgumentException("categoryCodeId must belong to CATEGORY group");
        }
        return code;
    }
}
