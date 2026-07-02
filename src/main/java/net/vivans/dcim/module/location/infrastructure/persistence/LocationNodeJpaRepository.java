package net.vivans.dcim.module.location.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.location.domain.model.LocationNode;
import net.vivans.dcim.module.location.domain.repository.LocationNodeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LocationNodeJpaRepository implements LocationNodeRepository {

    private final LocationNodeSpringDataRepository springDataRepository;

    @Override
    public LocationNode save(LocationNode node) {
        return springDataRepository.save(node);
    }

    @Override
    public Optional<LocationNode> findByCode(String code) {
        return springDataRepository.findById(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return springDataRepository.existsByCode(code);
    }

    @Override
    public List<LocationNode> findAll() {
        return springDataRepository.findAll();
    }

    @Override
    public boolean existsByParentIsNullAndName(String name) {
        return springDataRepository.existsByParentIsNullAndName(name);
    }

    @Override
    public boolean existsByParentAndName(LocationNode parent, String name) {
        return springDataRepository.existsByParentAndName(parent, name);
    }

    @Override
    public boolean existsByParentIsNullAndNameAndCodeNot(String name, String code) {
        return springDataRepository.existsByParentIsNullAndNameAndCodeNot(name, code);
    }

    @Override
    public boolean existsByParentAndNameAndCodeNot(LocationNode parent, String name, String code) {
        return springDataRepository.existsByParentAndNameAndCodeNot(parent, name, code);
    }
}
