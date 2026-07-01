package net.vivans.dcim.module.common.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CodeGroupTest {

    @Test
    void create_CodeGroup(){
        CodeGroup codeGroup = CodeGroup.createCodeGroup("Test_Type", "TEST");

        assertThat(codeGroup.getGroupKey()).isEqualTo("Test_Type");
        assertThat(codeGroup.getGroupName()).isEqualTo("TEST");
    }

    @Test
    void create_throwWhenGroupKeyIsBlank(){
        assertThatThrownBy(() -> CodeGroup.createCodeGroup("", "TEST"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GroupKey is required");
    }

    @Test
    void create_throwWhenGroupNameIsBlank(){
        assertThatThrownBy(() -> CodeGroup.createCodeGroup("TEST", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GroupName is required");
    }

    @Test
    void update_changesGroupKeyAndName() {
        CodeGroup codeGroup = CodeGroup.createCodeGroup("OLD_KEY", "OLD_NAME");

        codeGroup.update("NEW_KEY", "NEW_NAME");

        assertThat(codeGroup.getGroupKey()).isEqualTo("NEW_KEY");
        assertThat(codeGroup.getGroupName()).isEqualTo("NEW_NAME");
    }

    @Test
    void update_throwWhenGroupKeyIsBlank() {
        CodeGroup codeGroup = CodeGroup.createCodeGroup("OLD_KEY", "OLD_NAME");

        assertThatThrownBy(() -> codeGroup.update("", "NEW_NAME"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GroupKey is required");
    }

    @Test
    void update_throwWhenGroupNameIsBlank() {
        CodeGroup codeGroup = CodeGroup.createCodeGroup("OLD_KEY", "OLD_NAME");

        assertThatThrownBy(() -> codeGroup.update("NEW_KEY", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GroupName is required");
    }
}
