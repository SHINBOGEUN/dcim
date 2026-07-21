package net.vivans.dcim.module.devicemodel.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DeviceModelJpaRepository implements DeviceModelRepository {

    private final DeviceModelSpringDataRepository springDataRepository;

    @Override
    public DeviceModel save(DeviceModel deviceModel) {
        return springDataRepository.save(deviceModel);
    }

    @Override
    public void flush() {
        springDataRepository.flush();
    }

    @Override
    public Optional<DeviceModel> findById(Integer id) {
        return springDataRepository.findById(id);
    }

    @Override
    public List<DeviceModel> findAll(String name, String manufacturer) {
        return springDataRepository.findAllWithFilters(blankToNull(name), blankToNull(manufacturer));
    }

    @Override
    public boolean existsByNameAndManufacturer(String name, String manufacturer) {
        return springDataRepository.existsByNameAndManufacturer(name, manufacturer);
    }

    @Override
    public boolean existsByNameAndManufacturerAndIdNot(String name, String manufacturer, Integer id) {
        return springDataRepository.existsByNameAndManufacturerAndIdNot(name, manufacturer, id);
    }

    @Override
    public void delete(DeviceModel deviceModel) {
        springDataRepository.delete(deviceModel);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
