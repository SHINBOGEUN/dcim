package net.vivans.dcim.module.device.infrastructure.persistence;

import net.vivans.dcim.module.device.domain.model.Device;
import net.vivans.dcim.module.location.domain.model.LocationNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DeviceSpringDataRepository extends JpaRepository<Device, Integer> {

    @EntityGraph(attributePaths = {"deviceModel", "locationNode"})
    Optional<Device> findById(Integer id);

    @EntityGraph(attributePaths = {"deviceModel", "locationNode"})
    List<Device> findByLocationNode_Code(String locationNodeCode);

    @EntityGraph(attributePaths = {"deviceModel", "locationNode"})
    List<Device> findByLocationNode_CodeIn(Collection<String> locationNodeCodes);

    @EntityGraph(attributePaths = {"deviceModel", "locationNode"})
    @Query("SELECT d FROM Device d " +
            "WHERE (:modelId IS NULL OR d.deviceModel.id = :modelId) " +
            "AND (:locationNodeCode IS NULL OR d.locationNode.code = :locationNodeCode) " +
            "AND (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:enabled IS NULL OR d.enabled = :enabled)")
    Page<Device> findAllWithFilters(
            @Param("modelId") Integer modelId,
            @Param("locationNodeCode") String locationNodeCode,
            @Param("name") String name,
            @Param("enabled") Boolean enabled,
            Pageable pageable
    );

    boolean existsByLocationNodeAndName(LocationNode locationNode, String name);

    boolean existsByLocationNodeAndNameAndIdNot(LocationNode locationNode, String name, Integer id);
}
