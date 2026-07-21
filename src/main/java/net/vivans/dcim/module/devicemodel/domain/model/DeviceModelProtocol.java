package net.vivans.dcim.module.devicemodel.domain.model;

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
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.shared.persistence.BaseEntity;

@Entity
@Table(name = "device_model_protocol")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceModelProtocol extends BaseEntity {

    private static final String PROTOCOL_TYPE_GROUP_KEY = "PROTOCOL_TYPE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private DeviceModel deviceModel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "protocol_type_id", nullable = false)
    private CommonCode protocolType;

    private DeviceModelProtocol(DeviceModel deviceModel, CommonCode protocolType) {
        this.deviceModel = deviceModel;
        this.protocolType = protocolType;
    }

    public static DeviceModelProtocol of(DeviceModel deviceModel, CommonCode protocolType) {
        validateProtocolType(protocolType);
        return new DeviceModelProtocol(deviceModel, protocolType);
    }

    private static void validateProtocolType(CommonCode protocolType) {
        if (protocolType == null) {
            throw new IllegalArgumentException("protocolType is required");
        }
        if (!PROTOCOL_TYPE_GROUP_KEY.equals(protocolType.getCodeGroup().getGroupKey())) {
            throw new IllegalArgumentException("protocolType must belong to PROTOCOL_TYPE group");
        }
    }
}
