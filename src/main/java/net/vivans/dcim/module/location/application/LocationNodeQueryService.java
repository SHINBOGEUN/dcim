package net.vivans.dcim.module.location.application;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.location.domain.repository.LocationNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationNodeQueryService {

    private final LocationNodeRepository locationNodeRepository;


}
