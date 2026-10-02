package net.vivans.dcim.module.calculated.domain.repository;
import net.vivans.dcim.module.calculated.domain.model.CalculatedMetric;
import java.util.*;
public interface CalculatedMetricRepository {
    CalculatedMetric save(CalculatedMetric definition);
    Optional<CalculatedMetric> findById(Integer id);
    List<CalculatedMetric> findAllByCollectionEnabled(boolean enabled);
    List<CalculatedMetric> findAll();
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Integer id);
    void delete(CalculatedMetric definition);
}
