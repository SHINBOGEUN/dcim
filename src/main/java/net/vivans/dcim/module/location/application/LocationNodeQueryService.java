package net.vivans.dcim.module.location.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.common.domain.repository.CommonCodeRepository;
import net.vivans.dcim.module.location.api.dto.LocationNodeCreateRequest;
import net.vivans.dcim.module.location.api.dto.LocationNodeParentUpdateRequest;
import net.vivans.dcim.module.location.api.dto.LocationNodeResponse;
import net.vivans.dcim.module.location.api.dto.LocationNodeUpdateRequest;
import net.vivans.dcim.module.location.domain.model.LocationNode;
import net.vivans.dcim.module.location.domain.model.LocationNodeCodeGenerator;
import net.vivans.dcim.module.location.domain.repository.LocationNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationNodeQueryService {

    private static final int MAX_CODE_GENERATION_ATTEMPTS = 5;

    private final LocationNodeRepository locationNodeRepository;
    private final CommonCodeRepository commonCodeRepository;

    @Transactional
    public LocationNodeResponse createLocationNode(LocationNodeCreateRequest request) {
        CommonCode locationType = findLocationType(request.locationTypeId());

        LocationNode node;
        if (request.parentCode() == null || request.parentCode().isBlank()) {
            validateSiblingName(null, request.name(), null);
            node = LocationNode.createRoot(generateUniqueCode(), locationType, request.name());
        } else {
            LocationNode parent = locationNodeRepository.findByCode(request.parentCode())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "LocationNode not found: " + request.parentCode()));
            validateSiblingName(parent, request.name(), null);
            node = LocationNode.createChild(generateUniqueCode(), parent, locationType, request.name());
        }

        return LocationNodeResponse.from(locationNodeRepository.save(node));
    }

    @Transactional
    public LocationNodeResponse updateLocationNode(String code, LocationNodeUpdateRequest request) {
        LocationNode node = locationNodeRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("LocationNode not found: " + code));
        CommonCode locationType = findLocationType(request.locationTypeId());

        validateSiblingName(node.getParent(), request.name(), node.getCode());

        node.update(locationType, request.name());

        return LocationNodeResponse.from(locationNodeRepository.save(node));
    }

    @Transactional
    public LocationNodeResponse updateParentLocationNode(String code, LocationNodeParentUpdateRequest request) {
        LocationNode node = locationNodeRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("LocationNode not found: " + code));
        LocationNode newParent = resolveParent(request.parentCode());

        validateSiblingName(newParent, node.getName(), node.getCode());
        node.updateParent(newParent);

        return LocationNodeResponse.from(locationNodeRepository.save(node));
    }

    private LocationNode resolveParent(String parentCode) {
        if (parentCode == null || parentCode.isBlank()) {
            return null;
        }
        return locationNodeRepository.findByCode(parentCode)
                .orElseThrow(() -> new EntityNotFoundException("LocationNode not found: " + parentCode));
    }

    private CommonCode findLocationType(Integer locationTypeId) {
        return commonCodeRepository.findById(locationTypeId)
                .orElseThrow(() -> new EntityNotFoundException("CommonCode not found: " + locationTypeId));
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            String code = LocationNodeCodeGenerator.generate();
            if (!locationNodeRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("failed to generate unique location node code");
    }

    private void validateSiblingName(LocationNode parent, String name, String excludeCode) {
        boolean duplicate;
        if (parent == null) {
            duplicate = excludeCode == null
                    ? locationNodeRepository.existsByParentIsNullAndName(name)
                    : locationNodeRepository.existsByParentIsNullAndNameAndCodeNot(name, excludeCode);
        } else {
            duplicate = excludeCode == null
                    ? locationNodeRepository.existsByParentAndName(parent, name)
                    : locationNodeRepository.existsByParentAndNameAndCodeNot(parent, name, excludeCode);
        }
        if (duplicate) {
            throw new IllegalArgumentException("name already exists under parent");
        }
    }
}
