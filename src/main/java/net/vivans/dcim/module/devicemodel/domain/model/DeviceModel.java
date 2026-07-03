package net.vivans.dcim.module.devicemodel.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.vivans.dcim.shared.persistence.BaseEntity;

@Entity
@Table(name = "device_model")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceModel extends BaseEntity {
}
