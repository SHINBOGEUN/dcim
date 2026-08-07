package net.vivans.dcim.module.device.infrastructure.persistence;

import net.vivans.dcim.module.device.domain.model.DeviceSnmpInstance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceSnmpInstanceSpringDataRepository extends JpaRepository<DeviceSnmpInstance, Integer> {

    @EntityGraph(attributePaths = {
            "endpoint",
            "endpoint.device",
            "endpoint.protocolType",
            "endpoint.protocolType.codeGroup"
    })
    Optional<DeviceSnmpInstance> findByEndpointId(Integer endpointId);

    boolean existsByEndpointId(Integer endpointId);
}
