package net.vivans.dcim.module.devicemodel.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelSnmpPoint;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelSnmpPointRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DeviceModelSnmpPointJpaRepository implements DeviceModelSnmpPointRepository {

    private final DeviceModelSnmpPointSpringDataRepository springDataRepository;

    @Override
    public DeviceModelSnmpPoint save(DeviceModelSnmpPoint modelSnmpPoint) {
        return springDataRepository.save(modelSnmpPoint);
    }

    @Override
    public Optional<DeviceModelSnmpPoint> findByIdAndModelProtocolId(Integer id, Integer modelProtocolId) {
        return springDataRepository.findByIdAndModelProtocolId(id, modelProtocolId);
    }

    @Override
    public boolean existsByModelProtocolIdAndName(Integer modelProtocolId, String name) {
        return springDataRepository.existsByModelProtocolIdAndName(modelProtocolId, name);
    }

    @Override
    public boolean existsByModelProtocolIdAndNameAndIdNot(Integer modelProtocolId, String name, Integer id) {
        return springDataRepository.existsByModelProtocolIdAndNameAndIdNot(modelProtocolId, name, id);
    }
}
