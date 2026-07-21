package net.vivans.dcim.module.devicemodel.infrastructure.persistence;

import net.vivans.dcim.module.devicemodel.domain.model.DeviceModelSnmpPoint;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceModelSnmpPointSpringDataRepository extends JpaRepository<DeviceModelSnmpPoint, Integer> {

    @EntityGraph(attributePaths = {"modelProtocol", "modelProtocol.deviceModel", "modelProtocol.protocolType"})
    Optional<DeviceModelSnmpPoint> findByIdAndModelProtocolId(Integer id, Integer modelProtocolId);

    @EntityGraph(attributePaths = {"modelProtocol", "modelProtocol.deviceModel", "modelProtocol.protocolType"})
    List<DeviceModelSnmpPoint> findAllByModelProtocolIdOrderByIdAsc(Integer modelProtocolId);

    boolean existsByModelProtocolIdAndName(Integer modelProtocolId, String name);

    boolean existsByModelProtocolIdAndNameAndIdNot(Integer modelProtocolId, String name, Integer id);
}
