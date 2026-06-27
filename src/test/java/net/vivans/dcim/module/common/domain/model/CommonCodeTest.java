package net.vivans.dcim.module.common.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CommonCodeTest {

    @Test
    void create_CommonCode(){
        CodeGroup codeGroup = CodeGroup.creatCodeGroup("DEVICE_TYPE", "장비 유형");

        CommonCode code = CommonCode.createCommonCode(codeGroup, "PDU", "PDU", 0);

        assertThat(code.getCodeGroup()).isSameAs(codeGroup);
        assertThat(code.getCode()).isEqualTo("PDU");
        assertThat(code.getName()).isEqualTo("PDU");
        assertThat(code.getSortOrder()).isEqualTo(0);
    }

    @Test
    void create_throwsWhenCodeIsBlank(){
        CodeGroup codeGroup = CodeGroup.creatCodeGroup("DEVICE_TYPE", "장비 유형");

        assertThatThrownBy(() -> CommonCode.createCommonCode(codeGroup, "", "PDU", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("code is required");
    }

    @Test
    void create_throwsWhenNameIsBlank(){
        CodeGroup codeGroup = CodeGroup.creatCodeGroup("DEVICE_TYPE", "장비 유형");

        assertThatThrownBy(() -> CommonCode.createCommonCode(codeGroup, "PDU", "", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name is required");
    }

    @Test
    void create_throwsWhenCodeGroupIsBlank(){
        assertThatThrownBy(() -> CommonCode.createCommonCode(null, "PDU", "PDU", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("codeGroup is required");
    }

}
