package net.vivans.dcim.module.pue.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.vivans.dcim.module.device.domain.model.Device;

@Entity
@Table(name = "pue_definition_source", uniqueConstraints =
        @UniqueConstraint(name = "uk_pue_definition_source_alias", columnNames = {"pue_definition_id", "alias"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PueDefinitionSource {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pue_definition_id", nullable = false)
    private PueDefinition definition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private PueDefinitionSourceRole role;
    @Column(name = "alias", length = 32)
    private String alias;
    @Column(name = "protocol", length = 16)
    private String protocol;

    @Column(name = "point_name", nullable = false, length = 100)
    private String pointName;

    private PueDefinitionSource(PueDefinition definition, Device device, PueDefinitionSourceRole role, String pointName) {
        if (device == null || device.getId() == null) throw new IllegalArgumentException("PUE source device is required");
        if (pointName == null || pointName.isBlank()) throw new IllegalArgumentException("PUE source pointName is required");
        this.definition = definition;
        this.device = device;
        this.role = role;
        this.pointName = pointName.trim();
    }

    static PueDefinitionSource create(PueDefinition definition, Device device, PueDefinitionSourceRole role, String pointName) {
        return new PueDefinitionSource(definition, device, role, pointName);
    }
    static PueDefinitionSource createCalculated(PueDefinition definition, Device device, String alias, String pointName, String protocol) {
        PueDefinitionSource source = new PueDefinitionSource();
        source.definition = definition;
        source.device = device;
        source.alias = alias;
        source.protocol = protocol;
        source.pointName = pointName.trim();
        return source;
    }
    void updateCalculated(Device device, String pointName, String protocol) {
        this.device = device;
        this.pointName = pointName.trim();
        this.role = null;
        this.protocol = protocol;
    }

    void update(PueDefinitionSourceRole role, String pointName) {
        if (role == null) throw new IllegalArgumentException("PUE source role is required");
        if (pointName == null || pointName.isBlank()) throw new IllegalArgumentException("PUE source pointName is required");
        this.role = role;
        this.pointName = pointName.trim();
    }
}
