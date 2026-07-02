package net.vivans.dcim.module.location.domain.model;

import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocationNodeTest {

    private static final String ROOT_CODE = "K7mN2pQx9L";
    private static final String CHILD_CODE = "A1b2C3d4E5";

    @Test
    void create_LocationNodeRoot() {
        CodeGroup codeGroup = CodeGroup.createCodeGroup("LOCATION_TYPE", "장소 유형");
        CommonCode locationType = CommonCode.createCommonCode(codeGroup, "container", "컨테이너", 1);
        LocationNode node = LocationNode.createRoot(ROOT_CODE, locationType, "컨테이너 A");

        assertThat(node.getCode()).isEqualTo(ROOT_CODE);
        assertThat(node.getName()).isEqualTo("컨테이너 A");
        assertThat(node.getLocationType()).isEqualTo(locationType);
        assertThat(node.isRoot()).isTrue();
        assertThat(node.getParent()).isNull();
    }

    @Test
    void create_LocationNodeChild() {
        CodeGroup codeGroup = CodeGroup.createCodeGroup("LOCATION_TYPE", "장소 유형");
        CommonCode containerType = CommonCode.createCommonCode(codeGroup, "CONTAINER", "컨테이너", 1);
        CommonCode rowType = CommonCode.createCommonCode(codeGroup, "ROW", "열", 2);
        LocationNode parent = LocationNode.createRoot(ROOT_CODE, containerType, "컨테이너 A");

        LocationNode childNode = LocationNode.createChild(CHILD_CODE, parent, rowType, "A 열");

        assertThat(childNode.getCode()).isEqualTo(CHILD_CODE);
        assertThat(childNode.getName()).isEqualTo("A 열");
        assertThat(childNode.getLocationType()).isEqualTo(rowType);
        assertThat(childNode.getParent()).isEqualTo(parent);
        assertThat(childNode.isRoot()).isFalse();
    }

    @Test
    void update_LocationNode() {
        CodeGroup codeGroup = CodeGroup.createCodeGroup("LOCATION_TYPE", "장소 유형");
        CommonCode containerType = CommonCode.createCommonCode(codeGroup, "CONTAINER", "컨테이너", 1);
        CommonCode zoneType = CommonCode.createCommonCode(codeGroup, "ZONE", "존", 2);
        LocationNode node = LocationNode.createRoot(ROOT_CODE, containerType, "컨테이너 A");

        node.update(zoneType, "컨테이너 A (수정)");

        assertThat(node.getCode()).isEqualTo(ROOT_CODE);
        assertThat(node.getName()).isEqualTo("컨테이너 A (수정)");
        assertThat(node.getLocationType()).isEqualTo(zoneType);
    }

    @Test
    void createChild_throwsWhenParentIsNull() {
        CodeGroup codeGroup = CodeGroup.createCodeGroup("LOCATION_TYPE", "장소 유형");
        CommonCode rowType = CommonCode.createCommonCode(codeGroup, "ROW", "열", 1);

        assertThatThrownBy(() -> LocationNode.createChild(CHILD_CODE, null, rowType, "A 열"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("parent is required");
    }

    @Test
    void createRoot_throwsWhenCodeIsInvalid() {
        CodeGroup codeGroup = CodeGroup.createCodeGroup("LOCATION_TYPE", "장소 유형");
        CommonCode locationType = CommonCode.createCommonCode(codeGroup, "container", "컨테이너", 1);

        assertThatThrownBy(() -> LocationNode.createRoot("short", locationType, "컨테이너 A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("code is invalid");
    }
}
