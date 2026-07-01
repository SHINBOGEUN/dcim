package net.vivans.dcim.module.location.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.common.domain.repository.CommonCodeRepository;
import net.vivans.dcim.module.location.api.dto.LocationNodeRequest;
import net.vivans.dcim.module.location.api.dto.LocationNodeResponse;
import net.vivans.dcim.module.location.domain.model.LocationNode;
import net.vivans.dcim.module.location.domain.repository.LocationNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationNodeQueryService {

    private final LocationNodeRepository locationNodeRepository;
    private final CommonCodeRepository commonCodeRepository;

    @Transactional
    public LocationNodeResponse createLocationNode(LocationNodeRequest request) {
        CommonCode locationType = findLocationType(request.locationTypeId());

        if (request.code() != null && locationNodeRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("code already exists");
        }

        LocationNode node;
        if (request.parentId() == null) {
            node = LocationNode.createRoot(locationType, request.name(), request.code());
        } else {
            LocationNode parent = locationNodeRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "LocationNode not found: " + request.parentId()));
            node = LocationNode.createChild(parent, locationType, request.name(), request.code());
        }

        return LocationNodeResponse.from(locationNodeRepository.save(node));
    }

    @Transactional
    public LocationNodeResponse updateLocationNode(Integer id, LocationNodeRequest request) {
        LocationNode node = locationNodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("LocationNode not found: " + id));
        CommonCode locationType = findLocationType(request.locationTypeId());

        if (request.code() != null && locationNodeRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new IllegalArgumentException("code already exists");
        }

        node.update(locationType, request.name(), request.code());

        return LocationNodeResponse.from(locationNodeRepository.save(node));
    }

    private CommonCode findLocationType(Integer locationTypeId) {
        return commonCodeRepository.findById(locationTypeId)
                .orElseThrow(() -> new EntityNotFoundException("CommonCode not found: " + locationTypeId));
    }
}
