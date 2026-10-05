package net.vivans.dcim.module.device.infrastructure.persistence;

import net.vivans.dcim.module.device.domain.model.DevicePageModelPointSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DevicePageModelPointSettingRepository
        extends JpaRepository<DevicePageModelPointSetting, Integer> {

    List<DevicePageModelPointSetting> findAllByPageCode_IdAndDeviceModel_IdOrderBySortOrderAscIdAsc(
            Integer pageCodeId, Integer modelId);

    void deleteAllByPageCode_IdAndDeviceModel_Id(Integer pageCodeId, Integer modelId);
}
