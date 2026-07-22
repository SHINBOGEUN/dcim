package net.vivans.dcim.module.device.domain.repository;

import net.vivans.dcim.module.device.domain.model.Device;
import net.vivans.dcim.module.location.domain.model.LocationNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DeviceRepository {

    Device save(Device device);

    Optional<Device> findById(Integer id);

    Page<Device> findAll(
            Integer modelId,
            String locationNodeCode,
            String name,
            Boolean enabled,
            Pageable pageable
    );

    boolean existsByLocationNodeAndName(LocationNode locationNode, String name);

    boolean existsByLocationNodeAndNameAndIdNot(LocationNode locationNode, String name, Integer id);
}
