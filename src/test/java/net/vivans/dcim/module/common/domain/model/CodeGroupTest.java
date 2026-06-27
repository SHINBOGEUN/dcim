package net.vivans.dcim.module.common.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CodeGroupTest {

    @Test
    void create_CodeGroup(){
        CodeGroup codeGroup = CodeGroup.creatCodeGroup("Test_Type", "TEST");

        assertThat(codeGroup.getGroupKey()).isEqualTo("Test_Type");
        assertThat(codeGroup.getGroupName()).isEqualTo("TEST");
    }

    @Test
    void create_throwWhenGroupKeyIsBlank(){
        assertThatThrownBy(() -> CodeGroup.creatCodeGroup("", "TEST"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GroupKey is required");
    }

    @Test
    void create_throwWhenGroupNameIsBlank(){
        assertThatThrownBy(() -> CodeGroup.creatCodeGroup("TEST", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GroupName is required");
    }
}
