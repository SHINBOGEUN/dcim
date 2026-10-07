package net.vivans.dcim.module.device.api.dto;

import java.util.List;

public record DeviceRegistrationOptionsResponse(
        Integer modelId,
        String modelName,
        String deviceTypeCode,
        List<ProtocolOption> protocols
) {
    public record ProtocolOption(
            Integer protocolTypeId,
            String protocolCode,
            String protocolName,
            String configuration,
            int modelPointCount,
            boolean snmpInstanceRequired,
            List<CollectionGroupOption> collectionGroups
    ) {
    }

    public record CollectionGroupOption(
            Integer taskId, String taskName, Integer groupId, String groupName, String cronExpression
    ) {
    }
}
