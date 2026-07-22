package net.vivans.dcim.module.devicemodel.infrastructure.persistence;

import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeviceModelSpringDataRepository extends JpaRepository<DeviceModel, Integer> {

    @EntityGraph(attributePaths = {"deviceType", "protocols", "protocols.protocolType"})
    Optional<DeviceModel> findById(Integer id);

    @EntityGraph(attributePaths = {"deviceType", "protocols", "protocols.protocolType"})
    @Query("SELECT dm FROM DeviceModel dm " +
            "WHERE (:name IS NULL OR LOWER(dm.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:manufacturer IS NULL OR LOWER(dm.manufacturer) LIKE LOWER(CONCAT('%', :manufacturer, '%'))) " +
            "ORDER BY dm.manufacturer ASC, dm.name ASC")
    List<DeviceModel> findAllWithFilters(
            @Param("name") String name,
            @Param("manufacturer") String manufacturer
    );

    boolean existsByNameAndManufacturer(String name, String manufacturer);

    boolean existsByNameAndManufacturerAndIdNot(String name, String manufacturer, Integer id);
}
