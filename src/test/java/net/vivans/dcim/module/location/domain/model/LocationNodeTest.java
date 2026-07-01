package net.vivans.dcim.module.location.domain.model;


import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LocationNodeTest {

    @Test
    void create_LocationNodeRoot(){
        CodeGroup codeGroup = CodeGroup.createCodeGroup("LOCATION_TYPE", "장소 유형");
        CommonCode code = CommonCode.createCommonCode(codeGroup, "container", "컨테이너", 1);
        LocationNode node = LocationNode.createRoot(code, "컨테이너 A", "CA");

        assertThat(node.getCode()).isEqualTo("CA");
        assertThat(node.getName()).isEqualTo("컨테이너 A");
        assertThat(node.getLocationType()).isEqualTo(code);
    }
}
