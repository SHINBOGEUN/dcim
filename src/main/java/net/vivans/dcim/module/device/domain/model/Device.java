package net.vivans.dcim.module.device.domain.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.vivans.dcim.shared.persistence.BaseEntity;


@Entity
@Table(name = "devices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//매개 변수 없는 생성자를 protected로 생성하여 JPA 요구사항을 만족하면서도 외부에서 무분별한 객체 생성을 막기 위해 사용
public class Device extends BaseEntity {

    @Id
    private String deviceId;
}
