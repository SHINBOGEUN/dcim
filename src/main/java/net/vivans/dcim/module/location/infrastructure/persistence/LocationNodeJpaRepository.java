package net.vivans.dcim.module.location.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.location.domain.repository.LocationNodeRepository;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class LocationNodeJpaRepository implements LocationNodeRepository {
    private final LocationNodeSpringDataRepository springDataRepository;
}
