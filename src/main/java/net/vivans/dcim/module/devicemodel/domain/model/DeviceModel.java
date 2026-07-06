package net.vivans.dcim.module.devicemodel.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.vivans.dcim.shared.persistence.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "device_model")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String manufacturer;

    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "deviceModel", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<DeviceModelProtocol> protocols = new ArrayList<>();

    private DeviceModel(String name, String manufacturer, String description) {
        validateName(name);
        validateManufacturer(manufacturer);
        this.name = name;
        this.manufacturer = manufacturer;
        this.description = description;
    }

    public static DeviceModel create(String name, String manufacturer, String description) {
        return new DeviceModel(name, manufacturer, description);
    }

    public void update(String name, String manufacturer, String description) {
        validateName(name);
        validateManufacturer(manufacturer);
        this.name = name;
        this.manufacturer = manufacturer;
        this.description = description;
    }

    public void replaceProtocols(List<DeviceModelProtocol> newProtocols) {
        protocols.clear();
        for (DeviceModelProtocol protocol : newProtocols) {
            addProtocol(protocol);
        }
    }

    void addProtocol(DeviceModelProtocol protocol) {
        protocols.add(protocol);
    }

    public List<DeviceModelProtocol> getSortedProtocols() {
        List<DeviceModelProtocol> sorted = new ArrayList<>(protocols);
        sorted.sort((left, right) -> {
            Integer leftOrder = left.getSortOrder();
            Integer rightOrder = right.getSortOrder();
            if (leftOrder == null && rightOrder != null) {
                return 1;
            }
            if (leftOrder != null && rightOrder == null) {
                return -1;
            }
            if (leftOrder != null) {
                int orderCompare = leftOrder.compareTo(rightOrder);
                if (orderCompare != 0) {
                    return orderCompare;
                }
            }
            Integer leftId = left.getId();
            Integer rightId = right.getId();
            if (leftId == null && rightId == null) {
                return 0;
            }
            if (leftId == null) {
                return 1;
            }
            if (rightId == null) {
                return -1;
            }
            return leftId.compareTo(rightId);
        });
        return sorted;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
    }

    private static void validateManufacturer(String manufacturer) {
        if (manufacturer == null || manufacturer.isBlank()) {
            throw new IllegalArgumentException("manufacturer is required");
        }
    }
}
