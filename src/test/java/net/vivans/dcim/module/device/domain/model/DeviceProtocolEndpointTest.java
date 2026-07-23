package net.vivans.dcim.module.device.domain.model;

import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;
import net.vivans.dcim.module.location.domain.model.LocationNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeviceProtocolEndpointTest {

    @Test
    void create_withValidFields_succeeds() {
        Device device = device();
        CommonCode snmp = protocolType("snmp", "SNMP");

        DeviceProtocolEndpoint endpoint = DeviceProtocolEndpoint.create(
                device, snmp, "192.168.1.10", 161, true);

        assertThat(endpoint.getId()).isNull();
        assertThat(endpoint.getDevice()).isEqualTo(device);
        assertThat(endpoint.getProtocolType()).isEqualTo(snmp);
        assertThat(endpoint.getHost()).isEqualTo("192.168.1.10");
        assertThat(endpoint.getPort()).isEqualTo(161);
        assertThat(endpoint.isEnabled()).isTrue();
    }

    @Test
    void create_withoutDevice_throws() {
        assertThatThrownBy(() -> DeviceProtocolEndpoint.create(
                null, protocolType("snmp", "SNMP"), "192.168.1.10", 161, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("device is required");
    }

    @Test
    void create_withNonProtocolType_throws() {
        assertThatThrownBy(() -> DeviceProtocolEndpoint.create(
                device(), modelType(), "192.168.1.10", 161, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("protocolType must belong to PROTOCOL_TYPE group");
    }

    @Test
    void create_withBlankHost_throws() {
        assertThatThrownBy(() -> DeviceProtocolEndpoint.create(
                device(), protocolType("snmp", "SNMP"), "  ", 161, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("host is required");
    }

    @Test
    void create_withInvalidPort_throws() {
        assertThatThrownBy(() -> DeviceProtocolEndpoint.create(
                device(), protocolType("snmp", "SNMP"), "192.168.1.10", 0, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("port must be between 1 and 65535");
    }

    @Test
    void update_changesFields() {
        DeviceProtocolEndpoint endpoint = DeviceProtocolEndpoint.create(
                device(), protocolType("snmp", "SNMP"), "192.168.1.10", 161, true);
        CommonCode modbus = protocolType("modbus", "Modbus");

        endpoint.update(modbus, "10.0.0.1", 502, false);

        assertThat(endpoint.getProtocolType()).isEqualTo(modbus);
        assertThat(endpoint.getHost()).isEqualTo("10.0.0.1");
        assertThat(endpoint.getPort()).isEqualTo(502);
        assertThat(endpoint.isEnabled()).isFalse();
    }

    private Device device() {
        return Device.create(
                DeviceModel.create("AP8959", "APC", modelType(), null),
                LocationNode.createRoot(
                        Device.UNASSIGNED_LOCATION_CODE,
                        locationType(),
                        "미배정"
                ),
                "PDU-좌",
                null
        );
    }

    private CommonCode protocolType(String code, String name) {
        CodeGroup group = CodeGroup.createCodeGroup("PROTOCOL_TYPE", "Protocol Type");
        return CommonCode.createCommonCode(group, code, name, 1);
    }

    private CommonCode modelType() {
        CodeGroup group = CodeGroup.createCodeGroup("MODEL_TYPE", "Model Type");
        return CommonCode.createCommonCode(group, "PDU", "PDU", 1);
    }

    private CommonCode locationType() {
        CodeGroup group = CodeGroup.createCodeGroup("LOCATION_TYPE", "Location Type");
        return CommonCode.createCommonCode(group, "UNASSIGNED", "미배정", -1);
    }
}
