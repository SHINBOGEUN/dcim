package net.vivans.dcim.module.location.api.dto;

import net.vivans.dcim.module.location.domain.model.LocationNode;

public record LocationNodeResponse(
        String code,
        String parentCode,
        Integer locationTypeId,
        String name
) {

    public static LocationNodeResponse from(LocationNode node) {
        return new LocationNodeResponse(
                node.getCode(),
                node.getParent() != null ? node.getParent().getCode() : null,
                node.getLocationType().getId(),
                node.getName()
        );
    }
}
