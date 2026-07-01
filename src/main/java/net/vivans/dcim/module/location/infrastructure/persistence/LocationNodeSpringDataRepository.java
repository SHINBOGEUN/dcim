package net.vivans.dcim.module.location.infrastructure.persistence;

import net.vivans.dcim.module.location.domain.model.LocationNode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationNodeSpringDataRepository extends JpaRepository<Integer, LocationNode> {
}
