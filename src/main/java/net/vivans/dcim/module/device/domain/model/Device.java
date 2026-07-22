package net.vivans.dcim.module.device.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;
import net.vivans.dcim.module.location.domain.model.LocationNode;
import net.vivans.dcim.shared.persistence.BaseEntity;

@Entity
@Table(name = "devices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Device extends BaseEntity {

    /** 위치 미지정(선등록) 시 사용 */
    public static final String UNASSIGNED_LOCATION_CODE = "UNASSIGNED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private DeviceModel deviceModel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_node_code", nullable = false)
    private LocationNode locationNode;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean enabled;

    private Device(
            DeviceModel deviceModel,
            LocationNode locationNode,
            String name,
            String description,
            boolean enabled
    ) {
        validateDeviceModel(deviceModel);
        validateLocationNode(locationNode);
        validateName(name);
        this.deviceModel = deviceModel;
        this.locationNode = locationNode;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }

    public static Device create(
            DeviceModel deviceModel,
            LocationNode locationNode,
            String name,
            String description
    ) {
        return create(deviceModel, locationNode, name, description, true);
    }

    public static Device create(
            DeviceModel deviceModel,
            LocationNode locationNode,
            String name,
            String description,
            boolean enabled
    ) {
        return new Device(deviceModel, locationNode, name, description, enabled);
    }

    public void update(
            DeviceModel deviceModel,
            LocationNode locationNode,
            String name,
            String description,
            boolean enabled
    ) {
        validateDeviceModel(deviceModel);
        validateLocationNode(locationNode);
        validateName(name);
        this.deviceModel = deviceModel;
        this.locationNode = locationNode;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }

    public void reassignLocation(LocationNode locationNode) {
        validateLocationNode(locationNode);
        this.locationNode = locationNode;
    }

    public boolean isLocationUnassigned() {
        return UNASSIGNED_LOCATION_CODE.equals(locationNode.getCode());
    }

    private static void validateDeviceModel(DeviceModel deviceModel) {
        if (deviceModel == null) {
            throw new IllegalArgumentException("deviceModel is required");
        }
    }

    private static void validateLocationNode(LocationNode locationNode) {
        if (locationNode == null) {
            throw new IllegalArgumentException("locationNode is required");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
    }
}
