package net.vivans.dcim.module.query.application;

record AnalysisAggregation(String label, String window) {
    static AnalysisAggregation forInclusiveDays(long days) {
        if (days <= 7) return new AnalysisAggregation("raw", null);
        if (days <= 31) return new AnalysisAggregation("30m", "30m");
        if (days <= 92) return new AnalysisAggregation("3h", "3h");
        if (days <= 183) return new AnalysisAggregation("12h", "12h");
        return new AnalysisAggregation("1d", "1d");
    }
}
