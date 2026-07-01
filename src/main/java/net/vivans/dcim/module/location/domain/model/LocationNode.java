package net.vivans.dcim.module.location.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.shared.persistence.BaseEntity;

@Entity
@Table(name = "location_node")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationNode extends BaseEntity {

    private static final String LOCATION_TYPE_GROUP_KEY = "LOCATION_TYPE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private LocationNode parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_type_id", nullable = false)
    private CommonCode locationType;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String code;

    @Column(nullable = false)
    private int depth;

    private LocationNode(LocationNode parent, CommonCode locationType, String name,
                         String code, int depth
    ) {
        this.parent = parent;
        this.locationType = locationType;
        this.name = name;
        this.code = code;
        this.depth = depth;
    }

    public static LocationNode createRoot(
            CommonCode locationType,
            String name,
            String code
    ) {
        validateLocationType(locationType);
        validateName(name);
        validateCode(code);
        return new LocationNode(null, locationType, name, code, 0);
    }

    public static LocationNode createChild(
            LocationNode parent,
            CommonCode locationType,
            String name,
            String code
    ) {
        if (parent == null) {
            throw new IllegalArgumentException("parent is required");
        }
        validateLocationType(locationType);
        validateName(name);
        validateCode(code);
        return new LocationNode(parent, locationType, name, code, parent.depth + 1);
    }

    public void update(
            CommonCode locationType,
            String name,
            String code
    ) {
        validateLocationType(locationType);
        validateName(name);
        validateCode(code);
        this.locationType = locationType;
        this.name = name;
        this.code = code;
    }

    public boolean isRoot() {
        return parent == null;
    }

    private static void validateLocationType(CommonCode locationType) {
        if (locationType == null) {
            throw new IllegalArgumentException("locationType is required");
        }
        if (!LOCATION_TYPE_GROUP_KEY.equals(locationType.getCodeGroup().getGroupKey())) {
            throw new IllegalArgumentException("locationType must belong to LOCATION_TYPE group");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
    }

    private static void validateCode(String code) {
        if (code != null && code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
    }
}
