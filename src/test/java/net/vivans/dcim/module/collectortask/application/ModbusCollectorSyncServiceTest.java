package net.vivans.dcim.module.collectortask.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.vivans.dcim.module.collectortask.domain.model.CollectionTask;
import net.vivans.dcim.module.collectortask.domain.model.CollectionTaskGroup;
import net.vivans.dcim.module.collectortask.domain.repository.CollectionTaskRepository;
import net.vivans.dcim.module.collectortask.infrastructure.collector.CollectorJobClient;
import net.vivans.dcim.module.collectortask.infrastructure.collector.CollectorJobResponse;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ModbusCollectorSyncServiceTest {

    @Test
    void registersModbusJobWithTargets() {
        CollectorJobClient client = mock(CollectorJobClient.class);
        CollectionTaskRepository repository = mock(CollectionTaskRepository.class);
        CollectionTaskGroup group = group("{\"protocol\":\"modbus\",\"targets\":[{\"deviceId\":101}]}", null);
        CollectionGroupSpecService specs = mock(CollectionGroupSpecService.class);
        String spec = group.getGeneratedSpec();
        when(specs.generateJson(group)).thenReturn(spec);
        CollectorSyncService service = new CollectorSyncService(client, repository, new ObjectMapper(), specs);
        when(client.isEnabled()).thenReturn(true);
        when(client.register(group.getGeneratedSpec())).thenReturn(new CollectorJobResponse(
                "job-1", 2, 6, 10, "modbus", "0 */1 * * * *", true, 1,
                null, null, 0, null));

        service.syncGroupSpec(group, false);

        verify(client).register(group.getGeneratedSpec());
        verify(group).updateCollectorJobId("job-1");
    }

    @Test
    void removesOldJobWhenModbusHasNoTargets() {
        CollectorJobClient client = mock(CollectorJobClient.class);
        CollectionTaskGroup group = group("{\"protocol\":\"modbus\",\"targets\":[]}", "old-job");
        CollectionGroupSpecService specs = mock(CollectionGroupSpecService.class);
        String spec = group.getGeneratedSpec();
        when(specs.generateJson(group)).thenReturn(spec);
        CollectorSyncService service = new CollectorSyncService(
                client, mock(CollectionTaskRepository.class), new ObjectMapper(), specs);
        when(client.isEnabled()).thenReturn(true);

        service.syncGroupSpec(group, false);

        verify(client).delete("old-job");
        verify(client, never()).register(group.getGeneratedSpec());
        verify(group).updateCollectorJobId(null);
    }

    @Test
    void refreshesLegacyEmptySpecBeforeRegisteringExistingModbusGroup() {
        CollectorJobClient client = mock(CollectorJobClient.class);
        CollectionGroupSpecService specs = mock(CollectionGroupSpecService.class);
        String current = "{\"protocol\":\"modbus\",\"targets\":[{\"deviceId\":101}]}";
        CollectionTaskGroup group = group("{\"protocol\":\"modbus\",\"targets\":[]}", null);
        when(specs.generateJson(group)).thenReturn(current);
        when(client.isEnabled()).thenReturn(true);
        when(client.register(current)).thenReturn(new CollectorJobResponse(
                "job-1", 2, 6, 10, "modbus", "0 */1 * * * *", true, 1,
                null, null, 0, null));
        CollectorSyncService service = new CollectorSyncService(
                client, mock(CollectionTaskRepository.class), new ObjectMapper(), specs);

        service.syncGroupSpec(group, false);

        verify(group).updateGeneratedSpec(current);
        verify(client).register(current);
    }

    @Test
    void updatesExistingJobWhenModbusBitFieldsChange() {
        CollectorJobClient client = mock(CollectorJobClient.class);
        CollectionGroupSpecService specs = mock(CollectionGroupSpecService.class);
        CollectionTaskGroup group = group("{\"protocol\":\"modbus\",\"targets\":[{\"deviceId\":101}]}", "job-1");
        String updated = "{\"protocol\":\"modbus\",\"targets\":[{\"deviceId\":101,"
                + "\"points\":[{\"name\":\"RAW\",\"bitFields\":[{\"name\":\"VALVE\"}]}]}]}";
        when(specs.generateJson(group)).thenReturn(updated);
        when(client.isEnabled()).thenReturn(true);
        when(client.update("job-1", updated)).thenReturn(new CollectorJobResponse(
                "job-1", 2, 6, 10, "modbus", "0 */1 * * * *", true, 1,
                null, null, 0, null));
        CollectorSyncService service = new CollectorSyncService(
                client, mock(CollectionTaskRepository.class), new ObjectMapper(), specs);

        service.syncGroupSpec(group, false);

        verify(group).updateGeneratedSpec(updated);
        verify(client).update("job-1", updated);
        verify(client, never()).register(updated);
    }

    private static CollectionTaskGroup group(String spec, String collectorJobId) {
        CommonCode modbus = mock(CommonCode.class);
        when(modbus.getCode()).thenReturn("modbus");
        CollectionTask task = mock(CollectionTask.class);
        when(task.getScriptType()).thenReturn(modbus);
        when(task.getId()).thenReturn(2);
        when(task.isActive()).thenReturn(true);
        CollectionTaskGroup group = mock(CollectionTaskGroup.class);
        when(group.getTask()).thenReturn(task);
        when(group.getGeneratedSpec()).thenReturn(spec);
        when(group.getCollectorJobId()).thenReturn(collectorJobId);
        when(group.getId()).thenReturn(6);
        when(group.isActive()).thenReturn(true);
        return group;
    }
}
