package net.vivans.dcim.module.location.infrastructure.persistence;

import net.vivans.dcim.module.location.domain.model.LocationNode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationNodeSpringDataRepository extends JpaRepository<LocationNode, String> {

    boolean existsByCode(String code);

    boolean existsByParentIsNullAndName(String name);

    boolean existsByParentAndName(LocationNode parent, String name);

    boolean existsByParentIsNullAndNameAndCodeNot(String name, String code);

    boolean existsByParentAndNameAndCodeNot(LocationNode parent, String name, String code);
}
