package net.vivans.dcim.module.collectortask.application;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Collector에 보낼 본문과 운영 화면에서 사용할 수집원별 저장 대상의 대응 관계. */
public record CollectionGroupPlan(
        Object payload,
        List<String> skipped,
        Map<Integer, List<PointSource>> pointsBySourceDeviceId
) {
    public CollectionGroupPlan {
        skipped = List.copyOf(skipped);
        Map<Integer, List<PointSource>> copied = new LinkedHashMap<>();
        pointsBySourceDeviceId.forEach((id, points) -> copied.put(id, List.copyOf(points)));
        pointsBySourceDeviceId = Map.copyOf(copied);
    }

    public List<PointSource> pointsForSource(Integer sourceDeviceId) {
        return pointsBySourceDeviceId.getOrDefault(sourceDeviceId, List.of());
    }

    public boolean includesSource(Integer sourceDeviceId) {
        return !pointsForSource(sourceDeviceId).isEmpty();
    }

    public Set<String> pointNamesForSource(Integer sourceDeviceId) {
        Set<String> names = new LinkedHashSet<>();
        for (PointSource point : pointsForSource(sourceDeviceId)) {
            names.add(point.pointName());
        }
        return names;
    }

    public Map<String, String> unitsForSource(Integer sourceDeviceId) {
        Map<String, String> units = new LinkedHashMap<>();
        for (PointSource point : pointsForSource(sourceDeviceId)) {
            if (point.unit() != null) {
                units.put(point.pointName(), point.unit());
            }
        }
        return units;
    }

    public record PointSource(Integer storageDeviceId, String pointName, String unit) {
    }
}
