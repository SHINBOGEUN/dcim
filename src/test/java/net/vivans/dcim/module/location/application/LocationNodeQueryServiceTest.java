package net.vivans.dcim.module.location.application;

import jakarta.persistence.EntityNotFoundException;
import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.common.domain.repository.CommonCodeRepository;
import net.vivans.dcim.module.location.api.dto.LocationNodeCreateRequest;
import net.vivans.dcim.module.location.api.dto.LocationNodeParentUpdateRequest;
import net.vivans.dcim.module.location.api.dto.LocationNodeResponse;
import net.vivans.dcim.module.location.api.dto.LocationNodeUpdateRequest;
import net.vivans.dcim.module.location.domain.model.LocationNode;
import net.vivans.dcim.module.location.domain.model.LocationNodeCodeGenerator;
import net.vivans.dcim.module.location.domain.repository.LocationNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationNodeQueryServiceTest {

    private static final CommonCode CONTAINER_TYPE;
    private static final CommonCode ROW_TYPE;
    private static final CommonCode DEVICE_TYPE;

    static {
        CodeGroup locationGroup = CodeGroup.createCodeGroup("LOCATION_TYPE", "장소 유형");
        CONTAINER_TYPE = CommonCode.createCommonCode(locationGroup, "CONTAINER", "컨테이너", 1);
        ROW_TYPE = CommonCode.createCommonCode(locationGroup, "ROW", "열", 3);
        ReflectionTestUtils.setField(CONTAINER_TYPE, "id", 1);
        ReflectionTestUtils.setField(ROW_TYPE, "id", 3);

        CodeGroup deviceGroup = CodeGroup.createCodeGroup("DEVICE_TYPE", "장비 유형");
        DEVICE_TYPE = CommonCode.createCommonCode(deviceGroup, "pdu", "PDU", 1);
        ReflectionTestUtils.setField(DEVICE_TYPE, "id", 99);
    }

    @Mock
    private LocationNodeRepository locationNodeRepository;

    @Mock
    private CommonCodeRepository commonCodeRepository;

    @InjectMocks
    private LocationNodeQueryService locationNodeQueryService;

    private LocationNode container;
    private LocationNode row;

    @BeforeEach
    void setUp() {
        container = LocationNode.createRoot("TSTCNTR001", CONTAINER_TYPE, "컨테이너 A");
        row = LocationNode.createChild("TSTROW0001", container, ROW_TYPE, "A열");
    }

    @Test
    void createLocationNode_root_returnsResponseWithGeneratedCode() {
        when(commonCodeRepository.findById(1)).thenReturn(Optional.of(CONTAINER_TYPE));
        when(locationNodeRepository.existsByParentIsNullAndName("컨테이너 A")).thenReturn(false);
        when(locationNodeRepository.existsByCode(anyString())).thenReturn(false);
        when(locationNodeRepository.save(any(LocationNode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocationNodeResponse response = locationNodeQueryService.createLocationNode(
                new LocationNodeCreateRequest(null, 1, "컨테이너 A")
        );

        assertThat(response.code()).hasSize(LocationNodeCodeGenerator.CODE_LENGTH);
        assertThat(response.parentCode()).isNull();
        assertThat(response.locationTypeId()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("컨테이너 A");
        assertThat(response.children()).isEmpty();
        verify(locationNodeRepository).save(any(LocationNode.class));
    }

    @Test
    void createLocationNode_child_returnsResponseWithParentCode() {
        when(commonCodeRepository.findById(3)).thenReturn(Optional.of(ROW_TYPE));
        when(locationNodeRepository.findByCode("TSTCNTR001")).thenReturn(Optional.of(container));
        when(locationNodeRepository.existsByParentAndName(container, "A열")).thenReturn(false);
        when(locationNodeRepository.existsByCode(anyString())).thenReturn(false);
        when(locationNodeRepository.save(any(LocationNode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocationNodeResponse response = locationNodeQueryService.createLocationNode(
                new LocationNodeCreateRequest("TSTCNTR001", 3, "A열")
        );

        assertThat(response.parentCode()).isEqualTo("TSTCNTR001");
        assertThat(response.locationTypeId()).isEqualTo(3);
        assertThat(response.name()).isEqualTo("A열");
    }

    @Test
    void createLocationNode_throwsWhenParentNotFound() {
        when(commonCodeRepository.findById(3)).thenReturn(Optional.of(ROW_TYPE));
        when(locationNodeRepository.findByCode("UNKNOWN01")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationNodeQueryService.createLocationNode(
                new LocationNodeCreateRequest("UNKNOWN01", 3, "A열")
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("LocationNode not found: UNKNOWN01");

        verify(locationNodeRepository, never()).save(any());
    }

    @Test
    void createLocationNode_throwsWhenLocationTypeNotFound() {
        when(commonCodeRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationNodeQueryService.createLocationNode(
                new LocationNodeCreateRequest(null, 999, "컨테이너 A")
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("CommonCode not found: 999");
    }

    @Test
    void createLocationNode_throwsWhenLocationTypeIsNotLocationTypeGroup() {
        when(commonCodeRepository.findById(99)).thenReturn(Optional.of(DEVICE_TYPE));
        when(locationNodeRepository.existsByParentIsNullAndName("컨테이너 A")).thenReturn(false);
        when(locationNodeRepository.existsByCode(anyString())).thenReturn(false);

        assertThatThrownBy(() -> locationNodeQueryService.createLocationNode(
                new LocationNodeCreateRequest(null, 99, "컨테이너 A")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("locationType must belong to LOCATION_TYPE group");
    }

    @Test
    void createLocationNode_throwsWhenDuplicateRootName() {
        when(commonCodeRepository.findById(1)).thenReturn(Optional.of(CONTAINER_TYPE));
        when(locationNodeRepository.existsByParentIsNullAndName("컨테이너 A")).thenReturn(true);

        assertThatThrownBy(() -> locationNodeQueryService.createLocationNode(
                new LocationNodeCreateRequest(null, 1, "컨테이너 A")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name already exists under parent");
    }

    @Test
    void updateLocationNode_updatesNameAndType() {
        when(locationNodeRepository.findByCode("TSTCNTR001")).thenReturn(Optional.of(container));
        when(commonCodeRepository.findById(3)).thenReturn(Optional.of(ROW_TYPE));
        when(locationNodeRepository.existsByParentIsNullAndNameAndCodeNot("컨테이너 B", "TSTCNTR001"))
                .thenReturn(false);
        when(locationNodeRepository.save(container)).thenReturn(container);

        LocationNodeResponse response = locationNodeQueryService.updateLocationNode(
                "TSTCNTR001",
                new LocationNodeUpdateRequest(3, "컨테이너 B")
        );

        assertThat(response.code()).isEqualTo("TSTCNTR001");
        assertThat(response.name()).isEqualTo("컨테이너 B");
        assertThat(response.locationTypeId()).isEqualTo(3);
    }

    @Test
    void updateLocationNode_throwsWhenNodeNotFound() {
        when(locationNodeRepository.findByCode("UNKNOWN01")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationNodeQueryService.updateLocationNode(
                "UNKNOWN01",
                new LocationNodeUpdateRequest(1, "이름")
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("LocationNode not found: UNKNOWN01");
    }

    @Test
    void updateParentLocationNode_promotesToRoot() {
        when(locationNodeRepository.findByCode("TSTROW0001")).thenReturn(Optional.of(row));
        when(locationNodeRepository.existsByParentIsNullAndNameAndCodeNot("A열", "TSTROW0001"))
                .thenReturn(false);
        when(locationNodeRepository.save(row)).thenReturn(row);

        LocationNodeResponse response = locationNodeQueryService.updateParentLocationNode(
                "TSTROW0001",
                new LocationNodeParentUpdateRequest(null)
        );

        assertThat(response.parentCode()).isNull();
        assertThat(row.getParent()).isNull();
    }

    @Test
    void updateParentLocationNode_changesParent() {
        LocationNode anotherContainer = LocationNode.createRoot("TSTCNTR002", CONTAINER_TYPE, "컨테이너 B");

        when(locationNodeRepository.findByCode("TSTROW0001")).thenReturn(Optional.of(row));
        when(locationNodeRepository.findByCode("TSTCNTR002")).thenReturn(Optional.of(anotherContainer));
        when(locationNodeRepository.existsByParentAndNameAndCodeNot(anotherContainer, "A열", "TSTROW0001"))
                .thenReturn(false);
        when(locationNodeRepository.save(row)).thenReturn(row);

        LocationNodeResponse response = locationNodeQueryService.updateParentLocationNode(
                "TSTROW0001",
                new LocationNodeParentUpdateRequest("TSTCNTR002")
        );

        assertThat(response.parentCode()).isEqualTo("TSTCNTR002");
        assertThat(row.getParent()).isEqualTo(anotherContainer);
    }

    @Test
    void updateParentLocationNode_throwsWhenNewParentNotFound() {
        when(locationNodeRepository.findByCode("TSTROW0001")).thenReturn(Optional.of(row));
        when(locationNodeRepository.findByCode("UNKNOWN01")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationNodeQueryService.updateParentLocationNode(
                "TSTROW0001",
                new LocationNodeParentUpdateRequest("UNKNOWN01")
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("LocationNode not found: UNKNOWN01");
    }

    @Test
    void getLocationNodes_withoutFilter_returnsForest() {
        when(locationNodeRepository.findAll()).thenReturn(List.of(container, row));

        List<LocationNodeResponse> result = locationNodeQueryService.getLocationNodes(null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("TSTCNTR001");
        assertThat(result.get(0).children()).hasSize(1);
    }

    @Test
    void getLocationNodes_throwsWhenParentCodeNotFound() {
        when(locationNodeRepository.existsByCode("UNKNOWN01")).thenReturn(false);

        assertThatThrownBy(() -> locationNodeQueryService.getLocationNodes(null, "UNKNOWN01", null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("LocationNode not found: UNKNOWN01");
    }

    @Test
    void getLocationNodes_throwsWhenLocationTypeIdNotFound() {
        when(commonCodeRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationNodeQueryService.getLocationNodes(null, null, 999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("CommonCode not found: 999");
    }

    @Test
    void getLocationNodes_withParentCodeAndUnmatchedType_returnsRootWithMatchingChild() {
        when(locationNodeRepository.existsByCode("TSTCNTR001")).thenReturn(true);
        when(commonCodeRepository.findById(3)).thenReturn(Optional.of(ROW_TYPE));
        when(locationNodeRepository.findAll()).thenReturn(List.of(container, row));

        List<LocationNodeResponse> result = locationNodeQueryService.getLocationNodes(
                null,
                "TSTCNTR001",
                3
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("TSTCNTR001");
        assertThat(result.get(0).children()).hasSize(1);
        assertThat(result.get(0).children().get(0).code()).isEqualTo("TSTROW0001");
    }

    @Test
    void getLocationNodes_withParentCodeAndNoMatch_returnsEmptyList() {
        when(locationNodeRepository.existsByCode("TSTCNTR001")).thenReturn(true);
        when(locationNodeRepository.findAll()).thenReturn(List.of(container, row));

        List<LocationNodeResponse> result = locationNodeQueryService.getLocationNodes(
                "없는이름",
                "TSTCNTR001",
                null
        );

        assertThat(result).isEmpty();
    }

    @Test
    void getLocationNodes_withName_includesAncestorPath() {
        when(locationNodeRepository.findAll()).thenReturn(List.of(container, row));

        List<LocationNodeResponse> result = locationNodeQueryService.getLocationNodes(
                "A열",
                null,
                null
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("TSTCNTR001");
        assertThat(result.get(0).children().get(0).code()).isEqualTo("TSTROW0001");
    }
}
