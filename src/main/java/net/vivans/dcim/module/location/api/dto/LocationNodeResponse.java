package net.vivans.dcim.module.location.api.dto;

import net.vivans.dcim.module.location.domain.model.LocationNode;

public record LocationNodeResponse(
        Integer id,
        Integer parentId,
        Integer locationTypeId,
        String name,
        String code,
        int depth
) {

    public static LocationNodeResponse from(LocationNode node) {
        return new LocationNodeResponse(
                node.getId(),
                node.getParent() != null ? node.getParent().getId() : null,
                node.getLocationType().getId(),
                node.getName(),
                node.getCode(),
                node.getDepth()
        );
    }
}
