package net.vivans.dcim.module.calculated.infrastructure.persistence;
import net.vivans.dcim.module.calculated.domain.model.CalculatedMetric;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CalculatedMetricSpringDataRepository extends JpaRepository<CalculatedMetric, Integer> {
    @Override @EntityGraph(attributePaths = {"sources", "sources.device", "sources.device.deviceModel"}) Optional<CalculatedMetric> findById(Integer id);
    @EntityGraph(attributePaths = {"sources", "sources.device", "sources.device.deviceModel"}) List<CalculatedMetric> findAllByCollectionEnabledOrderByIdAsc(boolean collectionEnabled);
    @Override @EntityGraph(attributePaths = {"sources", "sources.device", "sources.device.deviceModel"}) List<CalculatedMetric> findAll();
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Integer id);
}
