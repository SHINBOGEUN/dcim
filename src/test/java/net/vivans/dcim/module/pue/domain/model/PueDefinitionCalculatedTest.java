package net.vivans.dcim.module.pue.domain.model;

import net.vivans.dcim.module.device.domain.model.Device;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PueDefinitionCalculatedTest {
    private static Device device(int id) {
        Device result = mock(Device.class);
        when(result.getId()).thenReturn(id);
        return result;
    }

    @Test
    void acceptsSnmpAndModbusPointsInOneFormula() {
        PueDefinition definition = PueDefinition.createCalculated("facility efficiency", null, true,
                "FACILITY / IT", "PUE", List.of(
                        new PueDefinition.CalculatedSourceDefinition(device(1), "FACILITY", "TOTAL_WT", "snmp"),
                        new PueDefinition.CalculatedSourceDefinition(device(2), "IT", "TOTAL_WT", "modbus")));
        assertThat(definition.getFormula()).isEqualTo("FACILITY / IT");
        assertThat(definition.calculatedSources()).hasSize(2);
    }

    @Test
    void rejectsUnknownAliasAndPushProtocol() {
        var source = new PueDefinition.CalculatedSourceDefinition(device(1), "A", "TOTAL_WT", "snmp");
        assertThatThrownBy(() -> PueDefinition.createCalculated("bad", null, true, "A + B", "W", List.of(source)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PueDefinition.createCalculated("bad", null, true, "A", "W", List.of(
                new PueDefinition.CalculatedSourceDefinition(device(1), "A", "TEMP", "mqtt"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void allowsDifferentPointsOnTheSameDevice() {
        Device sourceDevice = device(7);
        PueDefinition definition = PueDefinition.createCalculated("two points", null, true,
                "(TEMP + HUMIDITY) / 2", "", List.of(
                        new PueDefinition.CalculatedSourceDefinition(sourceDevice, "TEMP", "TEMP_C", "snmp"),
                        new PueDefinition.CalculatedSourceDefinition(sourceDevice, "HUMIDITY", "HUM", "snmp")));
        assertThat(definition.calculatedSources()).hasSize(2);
    }

    @Test
    void rejectsMalformedExpressionAndDuplicateAlias() {
        var source = new PueDefinition.CalculatedSourceDefinition(device(1), "A", "TEMP", "snmp");
        assertThatThrownBy(() -> PueDefinition.createCalculated("bad", null, true, "(A + 1", "", List.of(source)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PueDefinition.createCalculated("bad", null, true, "A + A", "", List.of(source, source)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
