package net.vivans.dcim.module.location.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.location.domain.model.LocationNode;
import net.vivans.dcim.module.location.domain.repository.LocationNodeRepository;
import org.springframework.stereotype.Repository;

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
    public Optional<LocationNode> findById(Integer id) {
        return springDataRepository.findById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return springDataRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Integer id) {
        return springDataRepository.existsByCodeAndIdNot(code, id);
    }
}
