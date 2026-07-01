package net.vivans.dcim.module.common.infrastructure.persistence;

import net.vivans.dcim.bootstrap.ManagerServerApplication;
import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.common.domain.repository.CodeGroupRepository;
import net.vivans.dcim.module.common.domain.repository.CommonCodeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ManagerServerApplication.class)
@ActiveProfiles("local")
@Transactional
class CommonCodeJpaRepositoryTest {

    @Autowired
    private CodeGroupRepository codeGroupRepository;

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Test
    void findByCodeGroupId_returnOnlyMatchingCodes() {
        CodeGroup deviceGroup = codeGroupRepository.save(CodeGroup.createCodeGroup("DEVICE_TYPE", "장비 유형"));
        CodeGroup locationGroup = codeGroupRepository.save(CodeGroup.createCodeGroup("LOCATION_TYPE", "위치 유형"));

        commonCodeRepository.save(CommonCode.createCommonCode(deviceGroup, "pdu", "PDU", 1));
        commonCodeRepository.save(CommonCode.createCommonCode(deviceGroup, "ups", "UPS", 2));
        commonCodeRepository.save(CommonCode.createCommonCode(locationGroup, "rack", "Rack", 1));

        assertThat(commonCodeRepository.findByCodeGroupId(deviceGroup.getId()))
                .hasSize(2)
                .extracting(CommonCode::getCode)
                .containsExactlyInAnyOrder("pdu", "ups");

        assertThat(commonCodeRepository.findByCodeGroupId(locationGroup.getId()))
                .hasSize(1)
                .extracting(CommonCode::getCode)
                .containsExactly("rack");
    }
}
