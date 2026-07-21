package net.vivans.dcim.module.devicemodel.domain.repository;

import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelSnmpPoint;

import java.util.List;
import java.util.Optional;

public interface DeviceModelSnmpPointRepository {

    DeviceModelSnmpPoint save(DeviceModelSnmpPoint modelSnmpPoint);

    Optional<DeviceModelSnmpPoint> findByIdAndModelProtocolId(Integer id, Integer modelProtocolId);

    List<DeviceModelSnmpPoint> findAllByModelProtocolIdOrderByIdAsc(Integer modelProtocolId);

    boolean existsByModelProtocolIdAndName(Integer modelProtocolId, String name);

    boolean existsByModelProtocolIdAndNameAndIdNot(Integer modelProtocolId, String name, Integer id);

    void delete(DeviceModelSnmpPoint modelSnmpPoint);
}
