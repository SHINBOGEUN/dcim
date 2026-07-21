package net.vivans.dcim.module.devicemodel.domain.repository;

import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;

import java.util.List;
import java.util.Optional;

public interface DeviceModelRepository {

    DeviceModel save(DeviceModel deviceModel);

    void flush();

    Optional<DeviceModel> findById(Integer id);

    List<DeviceModel> findAll(String name, String manufacturer);

    boolean existsByNameAndManufacturer(String name, String manufacturer);

    boolean existsByNameAndManufacturerAndIdNot(String name, String manufacturer, Integer id);

    void delete(DeviceModel deviceModel);
}
