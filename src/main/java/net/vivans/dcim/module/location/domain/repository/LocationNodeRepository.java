package net.vivans.dcim.module.location.domain.repository;

import net.vivans.dcim.module.location.domain.model.LocationNode;

import java.util.Optional;

public interface LocationNodeRepository {

    LocationNode save(LocationNode node);

    Optional<LocationNode> findById(Integer id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Integer id);
}
