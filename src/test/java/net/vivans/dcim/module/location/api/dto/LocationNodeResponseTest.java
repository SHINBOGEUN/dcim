package net.vivans.dcim.module.location.api.dto;

import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.location.domain.model.LocationNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocationNodeResponseTest {

    private static final CommonCode CONTAINER_TYPE;
    private static final CommonCode ZONE_TYPE;
    private static final CommonCode ROW_TYPE;

    static {
        CodeGroup codeGroup = CodeGroup.createCodeGroup("LOCATION_TYPE", "장소 유형");
        CONTAINER_TYPE = CommonCode.createCommonCode(codeGroup, "CONTAINER", "컨테이너", 1);
        ZONE_TYPE = CommonCode.createCommonCode(codeGroup, "ZONE", "존", 2);
        ROW_TYPE = CommonCode.createCommonCode(codeGroup, "ROW", "열", 3);
    }

    @Test
    void from_mapsNodeFields() {
        LocationNode container = LocationNode.createRoot("TSTCNTR001", CONTAINER_TYPE, "컨테이너 A");

        LocationNodeResponse response = LocationNodeResponse.from(container);

        assertThat(response.code()).isEqualTo("TSTCNTR001");
        assertThat(response.parentCode()).isNull();
        assertThat(response.name()).isEqualTo("컨테이너 A");
        assertThat(response.children()).isEmpty();
    }

    @Test
    void buildTree_withEmptyList_returnsEmpty() {
        assertThat(LocationNodeResponse.buildTree(List.of(), null)).isEmpty();
    }

    @Test
    void buildTree_nestsChildrenUnderParent() {
        LocationNode containerA = LocationNode.createRoot("TSTCNTR001", CONTAINER_TYPE, "컨테이너 A");
        LocationNode containerB = LocationNode.createRoot("TSTCNTR002", CONTAINER_TYPE, "컨테이너 B");
        LocationNode zone = LocationNode.createChild("TSTZONE001", containerA, ZONE_TYPE, "존 1");
        LocationNode row = LocationNode.createChild("TSTROW0001", zone, ROW_TYPE, "열 1");

        List<LocationNodeResponse> forest = LocationNodeResponse.buildTree(
                List.of(containerA, containerB, zone, row),
                null
        );

        assertThat(forest).hasSize(2);
        assertThat(forest.get(0).code()).isEqualTo("TSTCNTR001");
        assertThat(forest.get(0).children()).hasSize(1);
        assertThat(forest.get(0).children().get(0).code()).isEqualTo("TSTZONE001");
        assertThat(forest.get(0).children().get(0).children().get(0).code()).isEqualTo("TSTROW0001");
    }

    @Test
    void buildTree_withRootCode_returnsSubtree() {
        LocationNode container = LocationNode.createRoot("TSTCNTR001", CONTAINER_TYPE, "컨테이너 A");
        LocationNode zone = LocationNode.createChild("TSTZONE001", container, ZONE_TYPE, "존 1");
        LocationNode row = LocationNode.createChild("TSTROW0001", zone, ROW_TYPE, "열 1");

        List<LocationNodeResponse> subtree = LocationNodeResponse.buildTree(
                List.of(container, zone, row),
                "TSTCNTR001"
        );

        assertThat(subtree).hasSize(1);
        assertThat(subtree.get(0).children().get(0).children().get(0).code()).isEqualTo("TSTROW0001");
    }

    @Test
    void buildTree_whenRootNotInList_returnsEmpty() {
        LocationNode zone = LocationNode.createRoot("TSTZONE001", ZONE_TYPE, "존 1");

        List<LocationNodeResponse> subtree = LocationNodeResponse.buildTree(
                List.of(zone),
                "TSTCNTR001"
        );

        assertThat(subtree).isEmpty();
    }
}
