package net.vivans.dcim.module.query.infrastructure.influx;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

final class AnalysisFluxBuilder {

    private AnalysisFluxBuilder() {
    }

    static String buildRawQuery(
            String bucket,
            String measurement,
            List<Integer> deviceIds,
            List<String> pointNames,
            Instant start,
            Instant end
    ) {
        return """
                from(bucket: %s)
                  |> range(start: time(v: %s), stop: time(v: %s))
                  |> filter(fn: (r) => r["_measurement"] == %s)
                  |> filter(fn: (r) => r["_field"] == "value")
                  |> filter(fn: (r) => %s)
                  |> filter(fn: (r) => %s)
                  |> group(columns: ["device_id", "point_name"])
                  |> keep(columns: ["device_id", "point_name", "_value", "_time"])
                """.formatted(
                LastFluxBuilder.quote(bucket),
                LastFluxBuilder.quote(start.toString()),
                LastFluxBuilder.quote(end.toString()),
                LastFluxBuilder.quote(measurement),
                orEquals("device_id", deviceIds.stream().map(String::valueOf).toList()),
                orEquals("point_name", pointNames)
        );
    }

    static String buildStatsQuery(
            String bucket,
            String measurement,
            List<Integer> deviceIds,
            List<String> pointNames,
            Instant start,
            Instant end,
            String window
    ) {
        String filtered = """
                from(bucket: %s)
                  |> range(start: time(v: %s), stop: time(v: %s))
                  |> filter(fn: (r) => r["_measurement"] == %s)
                  |> filter(fn: (r) => r["_field"] == "value")
                  |> filter(fn: (r) => %s)
                  |> filter(fn: (r) => %s)
                  |> group(columns: ["device_id", "point_name"])
                """.formatted(
                LastFluxBuilder.quote(bucket),
                LastFluxBuilder.quote(start.toString()),
                LastFluxBuilder.quote(end.toString()),
                LastFluxBuilder.quote(measurement),
                orEquals("device_id", deviceIds.stream().map(String::valueOf).toList()),
                orEquals("point_name", pointNames)
        );
        return """
                data = %s
                mins = data |> aggregateWindow(every: %s, fn: min, createEmpty: false)
                  |> map(fn: (r) => ({r with statistic: "min"}))
                maxs = data |> aggregateWindow(every: %s, fn: max, createEmpty: false)
                  |> map(fn: (r) => ({r with statistic: "max"}))
                means = data |> aggregateWindow(every: %s, fn: mean, createEmpty: false)
                  |> map(fn: (r) => ({r with statistic: "mean"}))
                union(tables: [mins, maxs, means])
                  |> pivot(rowKey: ["_time", "device_id", "point_name"], columnKey: ["statistic"], valueColumn: "_value")
                  |> keep(columns: ["device_id", "point_name", "_time", "min", "max", "mean"])
                """.formatted(filtered, window, window, window);
    }

    private static String orEquals(String tag, List<String> values) {
        return values.stream()
                .map(value -> "r[%s] == %s".formatted(
                        LastFluxBuilder.quote(tag), LastFluxBuilder.quote(value)))
                .collect(Collectors.joining(" or "));
    }
}
