package net.vivans.dcim.module.device.domain.repository;

import net.vivans.dcim.module.device.domain.model.DeviceSnmpInstance;

import java.util.Optional;

public interface DeviceSnmpInstanceRepository {

    DeviceSnmpInstance save(DeviceSnmpInstance snmpInstance);

    Optional<DeviceSnmpInstance> findByEndpointId(Integer endpointId);

    boolean existsByEndpointId(Integer endpointId);

    void delete(DeviceSnmpInstance snmpInstance);
}
