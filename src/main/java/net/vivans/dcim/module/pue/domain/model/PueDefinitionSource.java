package net.vivans.dcim.module.pue.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.vivans.dcim.module.device.domain.model.Device;

@Entity
@Table(name = "calculated_metric_source", uniqueConstraints =
        @UniqueConstraint(name = "uk_calculated_metric_source_alias", columnNames = {"calculated_metric_id", "alias"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PueDefinitionSource {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "calculated_metric_id", nullable = false)
    private PueDefinition definition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "alias", nullable = false, length = 32)
    private String alias;
    @Column(name = "protocol", nullable = false, length = 16)
    private String protocol;

    @Column(name = "point_name", nullable = false, length = 100)
    private String pointName;

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
        this.protocol = protocol;
    }
}
