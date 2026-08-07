package net.vivans.dcim.module.device.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.vivans.dcim.shared.persistence.BaseEntity;

@Entity
@Table(name = "device_snmp_instance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceSnmpInstance extends BaseEntity {

    private static final String SNMP_PROTOCOL_CODE = "snmp";
    private static final int MIN_INSTANCE_ID = 1;

    @Id
    @Column(name = "endpoint_id")
    private Integer endpointId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "endpoint_id")
    private DeviceProtocolEndpoint endpoint;

    @Column(name = "instance_id", nullable = false)
    private int instanceId;

    private DeviceSnmpInstance(DeviceProtocolEndpoint endpoint, int instanceId) {
        validateEndpoint(endpoint);
        validateInstanceId(instanceId);
        this.endpoint = endpoint;
        this.instanceId = instanceId;
    }

    public static DeviceSnmpInstance create(DeviceProtocolEndpoint endpoint, int instanceId) {
        return new DeviceSnmpInstance(endpoint, instanceId);
    }

    public void update(int instanceId) {
        validateInstanceId(instanceId);
        this.instanceId = instanceId;
    }

    private static void validateEndpoint(DeviceProtocolEndpoint endpoint) {
        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint is required");
        }
        if (!SNMP_PROTOCOL_CODE.equals(endpoint.getProtocolType().getCode())) {
            throw new IllegalArgumentException("endpoint protocol must be snmp");
        }
    }

    private static void validateInstanceId(int instanceId) {
        if (instanceId < MIN_INSTANCE_ID) {
            throw new IllegalArgumentException("instanceId must be greater than or equal to 1");
        }
    }
}
