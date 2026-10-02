package net.vivans.dcim.module.query.infrastructure.influx;

import java.time.Instant;

final class PueFluxBuilder {

    private PueFluxBuilder() {
    }

    static String buildSeriesQuery(
            String bucket,
            String measurement,
            Integer definitionId,
            Instant start,
            Instant end,
            String window
    ) {
        return """
                from(bucket: %s)
                  |> range(start: time(v: %s), stop: time(v: %s))
                  |> filter(fn: (r) => r["_measurement"] == %s)
                  |> filter(fn: (r) => r["metric_kind"] == "calculated")
                  |> filter(fn: (r) => r["calculated_metric_id"] == %s)
                  |> filter(fn: (r) => r["_field"] == "value")
                  |> aggregateWindow(every: %s, fn: mean, createEmpty: false)
                  |> group()
                  |> pivot(rowKey: ["_time", "calculated_metric_id"], columnKey: ["_field"], valueColumn: "_value")
                  |> keep(columns: ["_time", "value"])
                  |> sort(columns: ["_time"])
                """.formatted(
                LastFluxBuilder.quote(bucket),
                LastFluxBuilder.quote(start.toString()),
                LastFluxBuilder.quote(end.toString()),
                LastFluxBuilder.quote(measurement),
                LastFluxBuilder.quote(String.valueOf(definitionId)),
                window
        );
    }
}
