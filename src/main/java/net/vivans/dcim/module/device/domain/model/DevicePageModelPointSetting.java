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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;
import net.vivans.dcim.shared.persistence.BaseEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "device_page_model_point_setting", uniqueConstraints = @UniqueConstraint(
        name = "uk_page_model_protocol_point",
        columnNames = {"page_code_id", "model_id", "protocol_type_id", "point_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DevicePageModelPointSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_code_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CommonCode pageCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DeviceModel deviceModel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "protocol_type_id", nullable = false)
    private CommonCode protocolType;

    /** Protocol-specific point id; polymorphic FK validated by the application layer. */
    @Column(name = "point_id", nullable = false)
    private Integer pointId;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    private DevicePageModelPointSetting(CommonCode pageCode, DeviceModel deviceModel,
                                        CommonCode protocolType, Integer pointId,
                                        boolean visible, int sortOrder) {
        this.pageCode = pageCode;
        this.deviceModel = deviceModel;
        this.protocolType = protocolType;
        this.pointId = pointId;
        this.visible = visible;
        this.sortOrder = sortOrder;
    }

    public static DevicePageModelPointSetting create(CommonCode pageCode, DeviceModel deviceModel,
                                                     CommonCode protocolType, Integer pointId,
                                                     boolean visible, int sortOrder) {
        if (pageCode == null || deviceModel == null || protocolType == null || pointId == null) {
            throw new IllegalArgumentException("page, model, protocol type and point id are required");
        }
        if (pointId <= 0) {
            throw new IllegalArgumentException("pointId must be positive");
        }
        if (sortOrder < 0) {
            throw new IllegalArgumentException("sortOrder must not be negative");
        }
        return new DevicePageModelPointSetting(pageCode, deviceModel, protocolType,
                pointId, visible, sortOrder);
    }
}
