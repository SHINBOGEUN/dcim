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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public List<LocationNodeResponse> getLocationNodes(String name, String parentCode, Integer locationTypeId) {
        String normalizedName = blankToNull(name);
        String normalizedParentCode = blankToNull(parentCode);

        if (normalizedParentCode != null && !locationNodeRepository.existsByCode(normalizedParentCode)) {
            throw new EntityNotFoundException("LocationNode not found: " + normalizedParentCode);
        }
        if (locationTypeId != null) {
            findLocationType(locationTypeId);
        }

        List<LocationNode> allNodes = locationNodeRepository.findAll();
        List<LocationNode> scopedNodes = normalizedParentCode == null
                ? allNodes
                : filterSubtree(normalizedParentCode, allNodes);

        List<LocationNode> nodes = applySearchFilters(
                scopedNodes,
                allNodes,
                normalizedName,
                locationTypeId,
                normalizedParentCode
        );

        return LocationNodeResponse.buildTree(nodes, normalizedParentCode);
    }

    private List<LocationNode> applySearchFilters(
            List<LocationNode> scopedNodes,
            List<LocationNode> allNodes,
            String name,
            Integer locationTypeId,
            String parentCode
    ) {
        if (name == null && locationTypeId == null) {
            return scopedNodes;
        }

        Map<String, String> parentByCode = buildParentByCode(allNodes);
        Map<String, List<LocationNode>> childrenByParentCode = buildChildrenByParentCode(allNodes);

        Set<String> scopedCodes = new HashSet<>();
        for (LocationNode node : scopedNodes) {
            scopedCodes.add(node.getCode());
        }

        Set<String> keepCodes = new HashSet<>();
        for (LocationNode node : scopedNodes) {
            if (!matchesName(node, name) || !matchesLocationType(node, locationTypeId)) {
                continue;
            }
            keepCodes.add(node.getCode());
            addAncestors(node.getCode(), parentByCode, keepCodes, scopedCodes);
            addDescendants(node.getCode(), childrenByParentCode, keepCodes, scopedCodes);
        }

        if (parentCode != null) {
            keepCodes.add(parentCode);
        }

        if (keepCodes.isEmpty()) {
            return List.of();
        }

        List<LocationNode> filtered = new ArrayList<>();
        for (LocationNode node : scopedNodes) {
            if (keepCodes.contains(node.getCode())) {
                filtered.add(node);
            }
        }
        return filtered;
    }

    private List<LocationNode> filterSubtree(String rootCode, List<LocationNode> allNodes) {
        Map<String, List<LocationNode>> childrenByParentCode = buildChildrenByParentCode(allNodes);

        Set<String> subtreeCodes = new HashSet<>();
        subtreeCodes.add(rootCode);
        collectDescendants(rootCode, childrenByParentCode, subtreeCodes);

        List<LocationNode> subtree = new ArrayList<>();
        for (LocationNode node : allNodes) {
            if (subtreeCodes.contains(node.getCode())) {
                subtree.add(node);
            }
        }
        return subtree;
    }

    private Map<String, String> buildParentByCode(List<LocationNode> nodes) {
        Map<String, String> parentByCode = new HashMap<>();
        for (LocationNode node : nodes) {
            if (node.getParent() == null) {
                parentByCode.put(node.getCode(), null);
                continue;
            }
            parentByCode.put(node.getCode(), node.getParent().getCode());
        }
        return parentByCode;
    }

    private Map<String, List<LocationNode>> buildChildrenByParentCode(List<LocationNode> nodes) {
        Map<String, List<LocationNode>> childrenByParentCode = new HashMap<>();
        for (LocationNode node : nodes) {
            if (node.getParent() == null) {
                continue;
            }
            String parentCode = node.getParent().getCode();
            List<LocationNode> children = childrenByParentCode.get(parentCode);
            if (children == null) {
                children = new ArrayList<>();
                childrenByParentCode.put(parentCode, children);
            }
            children.add(node);
        }
        return childrenByParentCode;
    }

    private void addAncestors(
            String code,
            Map<String, String> parentByCode,
            Set<String> keepCodes,
            Set<String> scopedCodes
    ) {
        String parentCode = parentByCode.get(code);
        while (parentCode != null) {
            if (!scopedCodes.contains(parentCode)) {
                return;
            }
            keepCodes.add(parentCode);
            parentCode = parentByCode.get(parentCode);
        }
    }

    private void addDescendants(
            String code,
            Map<String, List<LocationNode>> childrenByParentCode,
            Set<String> keepCodes,
            Set<String> scopedCodes
    ) {
        List<LocationNode> children = childrenByParentCode.get(code);
        if (children == null) {
            return;
        }
        for (LocationNode child : children) {
            if (!scopedCodes.contains(child.getCode())) {
                continue;
            }
            keepCodes.add(child.getCode());
            addDescendants(child.getCode(), childrenByParentCode, keepCodes, scopedCodes);
        }
    }

    private void collectDescendants(
            String code,
            Map<String, List<LocationNode>> childrenByParentCode,
            Set<String> subtreeCodes
    ) {
        List<LocationNode> children = childrenByParentCode.get(code);
        if (children == null) {
            return;
        }
        for (LocationNode child : children) {
            subtreeCodes.add(child.getCode());
            collectDescendants(child.getCode(), childrenByParentCode, subtreeCodes);
        }
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

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private boolean matchesName(LocationNode node, String name) {
        if (name == null) {
            return true;
        }
        return node.getName().toLowerCase().contains(name.toLowerCase());
    }

    private boolean matchesLocationType(LocationNode node, Integer locationTypeId) {
        if (locationTypeId == null) {
            return true;
        }
        return node.getLocationType().getId().equals(locationTypeId);
    }
}
