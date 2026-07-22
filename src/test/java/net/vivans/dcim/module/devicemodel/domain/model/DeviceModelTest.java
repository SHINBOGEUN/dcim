package net.vivans.dcim.module.devicemodel.domain.model;

import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeviceModelTest {

    @Test
    void create_withValidFields_succeeds() {
        CommonCode deviceType = modelType("SENSOR", "Sensor");
        DeviceModel model = DeviceModel.create("LHT65N-PIR", "Dragino", deviceType, "sensor");

        assertThat(model.getName()).isEqualTo("LHT65N-PIR");
        assertThat(model.getManufacturer()).isEqualTo("Dragino");
        assertThat(model.getDeviceType()).isEqualTo(deviceType);
        assertThat(model.getDescription()).isEqualTo("sensor");
        assertThat(model.getProtocols()).isEmpty();
    }

    @Test
    void create_withoutName_throws() {
        assertThatThrownBy(() -> DeviceModel.create(" ", "Dragino", modelType("SENSOR", "Sensor"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name is required");
    }

    @Test
    void create_withoutManufacturer_throws() {
        assertThatThrownBy(() -> DeviceModel.create("LHT65N-PIR", " ", modelType("SENSOR", "Sensor"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("manufacturer is required");
    }

    @Test
    void create_withoutDeviceType_throws() {
        assertThatThrownBy(() -> DeviceModel.create("LHT65N-PIR", "Dragino", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("deviceType is required");
    }

    @Test
    void create_withWrongDeviceTypeGroup_throws() {
        CodeGroup group = CodeGroup.createCodeGroup("LOCATION_TYPE", "Location Type");
        CommonCode rack = CommonCode.createCommonCode(group, "rack", "Rack", 1);

        assertThatThrownBy(() -> DeviceModel.create("LHT65N-PIR", "Dragino", rack, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("deviceType must belong to MODEL_TYPE group");
    }

    @Test
    void update_withoutManufacturer_throws() {
        DeviceModel model = DeviceModel.create("LHT65N-PIR", "Dragino", modelType("SENSOR", "Sensor"), null);

        assertThatThrownBy(() -> model.update("LHT65N-PIR", " ", modelType("SENSOR", "Sensor"), "updated"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("manufacturer is required");
    }

    @Test
    void replaceProtocols_replacesExistingProtocols() {
        DeviceModel model = DeviceModel.create("LHT65N-PIR", "Dragino", modelType("SENSOR", "Sensor"), null);
        CommonCode mqtt = protocolType("mqtt", "MQTT");
        CommonCode modbus = protocolType("modbus", "Modbus");

        model.replaceProtocols(java.util.List.of(
                DeviceModelProtocol.of(model, mqtt)
        ));

        assertThat(model.getProtocols()).hasSize(1);
        assertThat(model.getSortedProtocols().get(0).getProtocolType().getCode()).isEqualTo("mqtt");

        model.replaceProtocols(java.util.List.of(
                DeviceModelProtocol.of(model, modbus)
        ));

        assertThat(model.getProtocols()).hasSize(1);
        assertThat(model.getSortedProtocols().get(0).getProtocolType().getCode()).isEqualTo("modbus");
    }

    @Test
    void deviceModelProtocol_withoutProtocolType_throws() {
        DeviceModel model = DeviceModel.create("LHT65N-PIR", "Dragino", modelType("SENSOR", "Sensor"), null);

        assertThatThrownBy(() -> DeviceModelProtocol.of(model, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("protocolType is required");
    }

    @Test
    void deviceModelProtocol_withWrongGroup_throws() {
        DeviceModel model = DeviceModel.create("LHT65N-PIR", "Dragino", modelType("SENSOR", "Sensor"), null);
        CodeGroup group = CodeGroup.createCodeGroup("LOCATION_TYPE", "Location Type");
        CommonCode rack = CommonCode.createCommonCode(group, "rack", "Rack", 1);

        assertThatThrownBy(() -> DeviceModelProtocol.of(model, rack))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("protocolType must belong to PROTOCOL_TYPE group");
    }

    private CommonCode modelType(String code, String name) {
        CodeGroup group = CodeGroup.createCodeGroup("MODEL_TYPE", "Model Type");
        return CommonCode.createCommonCode(group, code, name, 1);
    }

    private CommonCode protocolType(String code, String name) {
        CodeGroup group = CodeGroup.createCodeGroup("PROTOCOL_TYPE", "Protocol Type");
        return CommonCode.createCommonCode(group, code, name, 1);
    }
}
