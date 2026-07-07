package net.vivans.dcim.module.devicemodel.domain.model;

import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeviceModelTest {

    @Test
    void create_withValidFields_succeeds() {
        DeviceModel model = DeviceModel.create("LHT65N-PIR", "Dragino", "sensor");

        assertThat(model.getName()).isEqualTo("LHT65N-PIR");
        assertThat(model.getManufacturer()).isEqualTo("Dragino");
        assertThat(model.getDescription()).isEqualTo("sensor");
        assertThat(model.getProtocols()).isEmpty();
    }

    @Test
    void create_withoutName_throws() {
        assertThatThrownBy(() -> DeviceModel.create(" ", "Dragino", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name is required");
    }

    @Test
    void replaceProtocols_replacesExistingProtocols() {
        DeviceModel model = DeviceModel.create("LHT65N-PIR", "Dragino", null);
        CommonCode mqtt = protocolType("mqtt", "MQTT");

        model.replaceProtocols(java.util.List.of(
                DeviceModelProtocol.of(model, mqtt)
        ));

        assertThat(model.getProtocols()).hasSize(1);
        assertThat(model.getSortedProtocols().get(0).getProtocolType().getCode()).isEqualTo("mqtt");
    }

    private CommonCode protocolType(String code, String name) {
        CodeGroup group = CodeGroup.createCodeGroup("PROTOCOL_TYPE", "Protocol Type");
        return CommonCode.createCommonCode(group, code, name, 1);
    }
}
