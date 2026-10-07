package net.vivans.dcim.module.query.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisAggregationTest {

    @Test
    void selectsRawForUpToSevenInclusiveDays() {
        assertThat(AnalysisAggregation.forInclusiveDays(1)).isEqualTo(new AnalysisAggregation("raw", null));
        assertThat(AnalysisAggregation.forInclusiveDays(7)).isEqualTo(new AnalysisAggregation("raw", null));
    }

    @Test
    void selectsThirtyMinuteWindowForEightToThirtyOneDays() {
        assertThat(AnalysisAggregation.forInclusiveDays(8)).isEqualTo(new AnalysisAggregation("30m", "30m"));
        assertThat(AnalysisAggregation.forInclusiveDays(31)).isEqualTo(new AnalysisAggregation("30m", "30m"));
    }

    @Test
    void selectsThreeAndTwelveHourWindowsAtBoundaries() {
        assertThat(AnalysisAggregation.forInclusiveDays(32)).isEqualTo(new AnalysisAggregation("3h", "3h"));
        assertThat(AnalysisAggregation.forInclusiveDays(92)).isEqualTo(new AnalysisAggregation("3h", "3h"));
        assertThat(AnalysisAggregation.forInclusiveDays(93)).isEqualTo(new AnalysisAggregation("12h", "12h"));
        assertThat(AnalysisAggregation.forInclusiveDays(183)).isEqualTo(new AnalysisAggregation("12h", "12h"));
        assertThat(AnalysisAggregation.forInclusiveDays(184)).isEqualTo(new AnalysisAggregation("1d", "1d"));
    }
}
