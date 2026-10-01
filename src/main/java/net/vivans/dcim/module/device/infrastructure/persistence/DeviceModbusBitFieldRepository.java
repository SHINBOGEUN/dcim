package net.vivans.dcim.module.device.infrastructure.persistence;

import net.vivans.dcim.module.device.domain.model.DeviceModbusBitField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceModbusBitFieldRepository extends JpaRepository<DeviceModbusBitField, Integer> {
    List<DeviceModbusBitField> findAllByReading_IdOrderByIdAsc(Integer readingId);
    List<DeviceModbusBitField> findAllByReading_IdInOrderByIdAsc(List<Integer> readingIds);
    void deleteAllByReading_Id(Integer readingId);
}
