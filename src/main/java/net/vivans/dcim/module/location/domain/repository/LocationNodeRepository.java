package net.vivans.dcim.module.location.domain.repository;

import net.vivans.dcim.module.location.domain.model.LocationNode;

import java.util.List;
import java.util.Optional;

public interface LocationNodeRepository {

    LocationNode save(LocationNode node);

    Optional<LocationNode> findByCode(String code);

    boolean existsByParent_Code(String parentCode);

    boolean existsByCode(String code);

    List<LocationNode> findAll();

    void delete(LocationNode node);

    void deleteAll(List<LocationNode> nodes);

    boolean existsByParentIsNullAndName(String name);

    boolean existsByParentAndName(LocationNode parent, String name);

    boolean existsByParentIsNullAndNameAndCodeNot(String name, String code);

    boolean existsByParentAndNameAndCodeNot(LocationNode parent, String name, String code);
}
