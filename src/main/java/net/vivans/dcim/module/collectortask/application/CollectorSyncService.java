package net.vivans.dcim.module.collectortask.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.vivans.dcim.module.collectortask.domain.model.CollectionTask;
import net.vivans.dcim.module.collectortask.domain.model.CollectionTaskGroup;
import net.vivans.dcim.module.collectortask.domain.repository.CollectionTaskRepository;
import net.vivans.dcim.module.collectortask.infrastructure.collector.CollectorJobClient;
import net.vivans.dcim.module.collectortask.infrastructure.collector.CollectorJobResponse;
import net.vivans.dcim.shared.exception.CollectorSyncException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorSyncService {

    private final CollectorJobClient collectorJobClient;
    private final CollectionTaskRepository collectionTaskRepository;
    private final ObjectMapper objectMapper;
    private final CollectionGroupSpecService collectionGroupSpecService;

    @Transactional
    public void syncGroupSpec(CollectionTaskGroup group) {
        syncGroupSpec(group, collectorJobClient.isFailFast());
    }

    @Transactional
    public void syncGroupSpec(CollectionTaskGroup group, boolean failFast) {
        if (!collectorJobClient.isEnabled()) {
            return;
        }
        CollectionTask task = group.getTask();
        String specJson = currentSpecJson(group);
        if (specJson == null || specJson.isBlank()) {
            return;
        }
        if (!isSupportedSpec(specJson, task)) {
            removeGroupJob(group, failFast);
            save(task);
            return;
        }
        if (!isCollectorEnabled(task, group)) {
            disableGroupJob(group, failFast);
            save(task);
            return;
        }
        String action = group.getCollectorJobId() == null ? "REGISTER" : "UPDATE";
        log.info("[COLLECTOR_SYNC_START] type=REGULAR action={} taskId={} groupId={}",
                action, task.getId(), group.getId());
        try {
            if (group.getCollectorJobId() == null) {
                CollectorJobResponse response = collectorJobClient.register(specJson);
                group.updateCollectorJobId(response.collectorJobId());
                log.info(
                        "[COLLECTOR_SYNC_END] type=REGULAR action=REGISTER taskId={} groupId={} collectorJobId={}",
                        task.getId(),
                        group.getId(),
                        response.collectorJobId()
                );
            } else {
                CollectorJobResponse response = collectorJobClient.update(group.getCollectorJobId(), specJson);
                group.updateCollectorJobId(response.collectorJobId());
                log.info(
                        "[COLLECTOR_SYNC_END] type=REGULAR action=UPDATE taskId={} groupId={} collectorJobId={}",
                        task.getId(),
                        group.getId(),
                        response.collectorJobId()
                );
            }
            save(task);
        } catch (Exception exception) {
            handleFailure("syncGroupSpec", task.getId(), group.getId(), failFast, exception);
        }
    }

    @Transactional
    public void syncGroupToggle(CollectionTaskGroup group) {
        syncGroupToggle(group, collectorJobClient.isFailFast());
    }

    @Transactional
    public void syncGroupToggle(CollectionTaskGroup group, boolean failFast) {
        if (!collectorJobClient.isEnabled()) {
            return;
        }
        CollectionTask task = group.getTask();
        if (!isSupportedTask(task) || !isSupportedSpec(currentSpecJson(group), task)) {
            removeGroupJob(group, failFast);
            save(task);
            return;
        }
        boolean enabled = isCollectorEnabled(task, group);
        if (group.getCollectorJobId() == null) {
            if (enabled && hasGeneratedSpec(group)) {
                syncGroupSpec(group, failFast);
            }
            return;
        }
        log.info("[COLLECTOR_SYNC_START] type=REGULAR action=TOGGLE taskId={} groupId={} enabled={}",
                task.getId(), group.getId(), enabled);
        try {
            collectorJobClient.toggle(group.getCollectorJobId(), enabled);
            log.info(
                    "[COLLECTOR_SYNC_END] type=REGULAR action=TOGGLE taskId={} groupId={} collectorJobId={} enabled={}",
                    task.getId(),
                    group.getId(),
                    group.getCollectorJobId(),
                    enabled
            );
        } catch (Exception exception) {
            handleFailure("syncGroupToggle", task.getId(), group.getId(), failFast, exception);
        }
    }

    @Transactional
    public void syncTaskToggle(CollectionTask task) {
        syncTaskToggle(task, collectorJobClient.isFailFast());
    }

    @Transactional
    public void syncTaskToggle(CollectionTask task, boolean failFast) {
        for (CollectionTaskGroup group : new ArrayList<>(task.getGroups())) {
            syncGroupToggle(group, failFast);
        }
    }

    @Transactional
    public void removeGroupJob(CollectionTaskGroup group) {
        removeGroupJob(group, collectorJobClient.isFailFast());
    }

    @Transactional
    public void removeGroupJob(CollectionTaskGroup group, boolean failFast) {
        if (!collectorJobClient.isEnabled()) {
            return;
        }
        if (group.getCollectorJobId() == null) {
            return;
        }
        CollectionTask task = group.getTask();
        String collectorJobId = group.getCollectorJobId();
        try {
            collectorJobClient.delete(collectorJobId);
            group.updateCollectorJobId(null);
            save(task);
            log.info(
                    "[COLLECTOR_SYNC_END] type=REGULAR action=DELETE taskId={} groupId={} collectorJobId={}",
                    task.getId(),
                    group.getId(),
                    collectorJobId
            );
        } catch (Exception exception) {
            handleFailure("removeGroupJob", task.getId(), group.getId(), failFast, exception);
        }
    }

    @Transactional
    public void removeTaskJobs(CollectionTask task) {
        removeTaskJobs(task, collectorJobClient.isFailFast());
    }

    @Transactional
    public void removeTaskJobs(CollectionTask task, boolean failFast) {
        for (CollectionTaskGroup group : new ArrayList<>(task.getGroups())) {
            removeGroupJob(group, failFast);
        }
    }

    @Transactional
    public void repushActiveGroups() {
        if (!collectorJobClient.isEnabled()) {
            log.info("collector sync disabled; skip startup repush");
            return;
        }
        List<CollectionTask> tasks = collectionTaskRepository.findAll(null, null, null);
        for (CollectionTask task : tasks) {
            if (!task.isActive()) {
                continue;
            }
            if (!isSupportedTask(task)) {
                continue;
            }
            for (CollectionTaskGroup group : new ArrayList<>(task.getGroups())) {
                if (!group.isActive() || !hasGeneratedSpec(group) && !isModbusTask(task)) {
                    continue;
                }
                repushGroupInternal(group);
            }
        }
    }

    /**
     * Collector 인스턴스는 그대로 두고, DB에 저장된 활성 Job 정의를 안전하게 다시 반영한다.
     * 기존 collectorJobId가 있으면 UPDATE, 없으면 REGISTER를 사용하므로 중복 Job을 만들지 않는다.
     */
    @Transactional
    public int reconcileActiveGroups() {
        if (!collectorJobClient.isEnabled()) {
            log.info("collector sync disabled; skip manual reconciliation");
            return 0;
        }
        int synchronizedCount = 0;
        List<CollectionTask> tasks = collectionTaskRepository.findAll(null, null, null);
        for (CollectionTask task : tasks) {
            if (!task.isActive() || !isSupportedTask(task)) {
                continue;
            }
            for (CollectionTaskGroup group : new ArrayList<>(task.getGroups())) {
                if (!group.isActive() || !hasGeneratedSpec(group) && !isModbusTask(task)) {
                    continue;
                }
                syncGroupSpec(group, false);
                synchronizedCount++;
            }
        }
        log.info("collector manual reconciliation completed: synchronizedGroups={}", synchronizedCount);
        return synchronizedCount;
    }

    @Transactional
    public void repushGroup(CollectionTaskGroup group) {
        repushGroupInternal(group);
    }

    private void repushGroupInternal(CollectionTaskGroup group) {
        CollectionTask task = group.getTask();
        String specJson = currentSpecJson(group);
        if (specJson == null || specJson.isBlank() || !isSupportedSpec(specJson, task)) {
            return;
        }
        try {
            CollectorJobResponse response = collectorJobClient.register(specJson);
            group.updateCollectorJobId(response.collectorJobId());
            save(task);
            log.info(
                    "collector job repushed: taskId={}, groupId={}, collectorJobId={}",
                    task.getId(),
                    group.getId(),
                    response.collectorJobId()
            );
        } catch (Exception exception) {
            handleFailure("repushGroup", task.getId(), group.getId(), false, exception);
        }
    }

    private void disableGroupJob(CollectionTaskGroup group, boolean failFast) {
        if (group.getCollectorJobId() == null) {
            return;
        }
        CollectionTask task = group.getTask();
        try {
            collectorJobClient.toggle(group.getCollectorJobId(), false);
            log.info(
                    "collector job disabled: taskId={}, groupId={}, collectorJobId={}",
                    task.getId(),
                    group.getId(),
                    group.getCollectorJobId()
            );
        } catch (Exception exception) {
            handleFailure("disableGroupJob", task.getId(), group.getId(), failFast, exception);
        }
    }

    private void handleFailure(
            String operation,
            Integer taskId,
            Integer groupId,
            boolean failFast,
            Exception exception
    ) {
        log.error("[COLLECTOR_SYNC_ERROR] type=REGULAR action={} taskId={} groupId={} exception={} message={}",
                operation, taskId, groupId, exception.getClass().getSimpleName(), exception.getMessage());
        collectorJobClient.logFailure(operation, taskId, groupId, exception);
        if (failFast) {
            throw new CollectorSyncException(
                    "collector sync failed: " + operation + " (taskId=" + taskId + ", groupId=" + groupId + ")",
                    exception
            );
        }
    }

    private void save(CollectionTask task) {
        collectionTaskRepository.save(task);
    }

    private static boolean isCollectorEnabled(CollectionTask task, CollectionTaskGroup group) {
        return task.isActive() && group.isActive();
    }

    private static boolean hasGeneratedSpec(CollectionTaskGroup group) {
        return group.getGeneratedSpec() != null && !group.getGeneratedSpec().isBlank();
    }

    private String currentSpecJson(CollectionTaskGroup group) {
        if (isModbusTask(group.getTask())) {
            String generated = collectionGroupSpecService.generateJson(group);
            if (!Objects.equals(generated, group.getGeneratedSpec())) {
                group.updateGeneratedSpec(generated);
            }
            return generated;
        }
        return group.getGeneratedSpec();
    }

    private static boolean isModbusTask(CollectionTask task) {
        return task.getScriptType() != null && CollectionGroupSpecService.MODBUS_PROTOCOL_CODE
                .equalsIgnoreCase(task.getScriptType().getCode());
    }

    private static boolean isSupportedTask(CollectionTask task) {
        if (task.getScriptType() == null) {
            return false;
        }
        return CollectionGroupSpecService.isCollectorProtocol(task.getScriptType().getCode());
    }

    private boolean isSupportedSpec(String specJson, CollectionTask task) {
        if (specJson == null || specJson.isBlank() || !isSupportedTask(task)) {
            return false;
        }
        try {
            JsonNode spec = objectMapper.readTree(specJson);
            String protocol = spec.path("protocol").asText();
            if (!task.getScriptType().getCode().equalsIgnoreCase(protocol)) {
                return false;
            }
            return !CollectionGroupSpecService.MODBUS_PROTOCOL_CODE.equalsIgnoreCase(protocol)
                    || spec.path("targets").isArray() && !spec.path("targets").isEmpty();
        } catch (Exception exception) {
            log.warn("failed to parse generated spec for collector sync", exception);
            return false;
        }
    }
}
