package net.vivans.dcim.module.collectortask.application;

import net.vivans.dcim.module.collectortask.domain.model.CollectionTask;
import net.vivans.dcim.module.collectortask.domain.model.CollectionTaskGroup;
import net.vivans.dcim.module.collectortask.domain.repository.CollectionTaskRepository;
import net.vivans.dcim.module.device.domain.repository.DeviceRepository;
import net.vivans.dcim.module.device.domain.repository.DeviceModbusReadingRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CollectionScriptSyncServiceTest {

    @Test
    void changedGroupSpecIsSentToCollectorImmediately() {
        CollectionTaskRepository tasks = mock(CollectionTaskRepository.class);
        CollectionGroupSpecService specs = mock(CollectionGroupSpecService.class);
        CollectorSyncService collector = mock(CollectorSyncService.class);
        CollectionTask task = mock(CollectionTask.class);
        CollectionTaskGroup group = mock(CollectionTaskGroup.class);
        when(tasks.findAllByModelId(5)).thenReturn(List.of(task));
        when(task.getGroups()).thenReturn(List.of(group));
        when(group.getGeneratedSpec()).thenReturn("old");
        when(specs.generateJson(group)).thenReturn("updated");
        CollectionScriptSyncService service = new CollectionScriptSyncService(tasks, specs,
                mock(DeviceRepository.class), mock(DeviceModbusReadingRepository.class), collector);

        service.regenerateByModelId(5);

        verify(group).updateGeneratedSpec("updated");
        verify(collector).syncGroupSpec(group, false);
    }

    @Test
    void unchangedGroupSpecDoesNotResetCollectorSchedule() {
        CollectionTaskRepository tasks = mock(CollectionTaskRepository.class);
        CollectionGroupSpecService specs = mock(CollectionGroupSpecService.class);
        CollectorSyncService collector = mock(CollectorSyncService.class);
        CollectionTask task = mock(CollectionTask.class);
        CollectionTaskGroup group = mock(CollectionTaskGroup.class);
        when(tasks.findAllByModelId(5)).thenReturn(List.of(task));
        when(task.getGroups()).thenReturn(List.of(group));
        when(group.getGeneratedSpec()).thenReturn("same");
        when(specs.generateJson(group)).thenReturn("same");
        CollectionScriptSyncService service = new CollectionScriptSyncService(tasks, specs,
                mock(DeviceRepository.class), mock(DeviceModbusReadingRepository.class), collector);

        service.regenerateByModelId(5);

        verify(collector, never()).syncGroupSpec(group, false);
    }
}
