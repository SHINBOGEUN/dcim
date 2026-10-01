package net.vivans.dcim.module.operations.api.dto;

import java.time.Instant;

/**
 * 수집 그룹 하나(Collector의 job 하나)에 대한 현재 실행 상태와 마지막 실패 이력.
 * 운영 콘솔에서 "언제부터, 몇 번, 왜 실패하고 있는지"를 단순 로그가 아닌 API 응답으로 확인하기 위함이다.
 * <p>
 * status:
 * <ul>
 *   <li>{@code NOT_SYNCED} - 아직 Collector에 등록되지 않음(collectorJobId 없음)</li>
 *   <li>{@code UNKNOWN} - collectorJobId는 있으나 Collector가 응답하지 않거나 해당 job을 찾을 수 없음</li>
 *   <li>{@code FAILING} - 최근 tick에서 실패가 발생했고 아직 정상 tick으로 복구되지 않음</li>
 *   <li>{@code NORMAL} - 현재 연속 실패가 없음(과거 실패 이력은 별도 필드로 유지)</li>
 * </ul>
 */
public record CollectionJobHealthResponse(
        Integer taskId,
        String taskName,
        Integer groupId,
        String groupName,
        Integer modelId,
        String modelName,
        String protocol,
        String collectorJobId,
        boolean enabled,
        int targetCount,
        String status,
        Instant lastSuccessAt,
        Instant lastFailureAt,
        int consecutiveFailureCount,
        String lastFailureReason
) {
}
