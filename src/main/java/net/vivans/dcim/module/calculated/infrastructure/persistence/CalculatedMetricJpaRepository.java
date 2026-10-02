package net.vivans.dcim.module.calculated.infrastructure.persistence;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.calculated.domain.model.CalculatedMetric;
import net.vivans.dcim.module.calculated.domain.repository.CalculatedMetricRepository;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository @RequiredArgsConstructor
public class CalculatedMetricJpaRepository implements CalculatedMetricRepository {
 private final CalculatedMetricSpringDataRepository repository;
 public CalculatedMetric save(CalculatedMetric d){return repository.save(d);} public Optional<CalculatedMetric> findById(Integer id){return repository.findById(id);}
 public List<CalculatedMetric> findAllByCollectionEnabled(boolean enabled){return repository.findAllByCollectionEnabledOrderByIdAsc(enabled);}
 public List<CalculatedMetric> findAll(){return repository.findAll();}
 public boolean existsByName(String n){return repository.existsByName(n);}
 public boolean existsByNameAndIdNot(String n,Integer id){return repository.existsByNameAndIdNot(n,id);} public void delete(CalculatedMetric d){repository.delete(d);}
}
