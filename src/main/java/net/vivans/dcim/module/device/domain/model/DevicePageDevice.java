package net.vivans.dcim.module.device.domain.model;

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
import net.vivans.dcim.shared.persistence.BaseEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "device_page_device", uniqueConstraints = @UniqueConstraint(
        name = "uk_device_page_device_page_device", columnNames = {"page_code_id", "device_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DevicePageDevice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_code_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CommonCode pageCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Device device;

    private DevicePageDevice(CommonCode pageCode, Device device) {
        this.pageCode = pageCode;
        this.device = device;
    }

    public static DevicePageDevice create(CommonCode pageCode, Device device) {
        return new DevicePageDevice(pageCode, device);
    }
}
