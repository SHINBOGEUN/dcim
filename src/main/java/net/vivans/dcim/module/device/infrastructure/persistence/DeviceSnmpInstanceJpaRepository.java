package net.vivans.dcim.module.device.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.device.domain.model.DeviceSnmpInstance;
import net.vivans.dcim.module.device.domain.repository.DeviceSnmpInstanceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DeviceSnmpInstanceJpaRepository implements DeviceSnmpInstanceRepository {

    private final DeviceSnmpInstanceSpringDataRepository springDataRepository;

    @Override
    public DeviceSnmpInstance save(DeviceSnmpInstance snmpInstance) {
        return springDataRepository.save(snmpInstance);
    }

    @Override
    public Optional<DeviceSnmpInstance> findByEndpointId(Integer endpointId) {
        return springDataRepository.findByEndpointId(endpointId);
    }

    @Override
    public boolean existsByEndpointId(Integer endpointId) {
        return springDataRepository.existsByEndpointId(endpointId);
    }

    @Override
    public void delete(DeviceSnmpInstance snmpInstance) {
        springDataRepository.delete(snmpInstance);
    }
}
