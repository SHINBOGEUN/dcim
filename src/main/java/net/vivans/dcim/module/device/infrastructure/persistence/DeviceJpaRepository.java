package net.vivans.dcim.module.device.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.device.domain.model.Device;
import net.vivans.dcim.module.device.domain.repository.DeviceRepository;
import net.vivans.dcim.module.location.domain.model.LocationNode;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DeviceJpaRepository implements DeviceRepository {

    private final DeviceSpringDataRepository springDataRepository;

    @Override
    public Device save(Device device) {
        return springDataRepository.save(device);
    }

    @Override
    public Optional<Device> findById(Integer id) {
        return springDataRepository.findById(id);
    }

    @Override
    public boolean existsByLocationNodeAndName(LocationNode locationNode, String name) {
        return springDataRepository.existsByLocationNodeAndName(locationNode, name);
    }

    @Override
    public boolean existsByLocationNodeAndNameAndIdNot(LocationNode locationNode, String name, Integer id) {
        return springDataRepository.existsByLocationNodeAndNameAndIdNot(locationNode, name, id);
    }
}
