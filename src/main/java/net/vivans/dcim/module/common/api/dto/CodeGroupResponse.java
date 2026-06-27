package net.vivans.dcim.module.common.api.dto;

import net.vivans.dcim.module.common.domain.model.CodeGroup;

public record CodeGroupResponse(
        Integer id,
        String groupKey,
        String groupName
) {

    public static CodeGroupResponse from(CodeGroup codeGroup) {
        return new CodeGroupResponse(
                codeGroup.getId(),
                codeGroup.getGroupKey(),
                codeGroup.getGroupName()
        );
    }
}
