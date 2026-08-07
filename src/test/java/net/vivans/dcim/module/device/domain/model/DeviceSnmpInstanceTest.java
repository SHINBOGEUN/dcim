package net.vivans.dcim.module.device.domain.model;

import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;
import net.vivans.dcim.module.location.domain.model.LocationNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeviceSnmpInstanceTest {

    @Test
    void create_withValidFields_succeeds() {
        DeviceProtocolEndpoint endpoint = snmpEndpoint();

        DeviceSnmpInstance snmpInstance = DeviceSnmpInstance.create(endpoint, 1);

        assertThat(snmpInstance.getEndpoint()).isEqualTo(endpoint);
        assertThat(snmpInstance.getInstanceId()).isEqualTo(1);
    }

    @Test
    void create_withNonSnmpEndpoint_throws() {
        DeviceProtocolEndpoint endpoint = DeviceProtocolEndpoint.create(
                device(),
                protocolType("modbus", "Modbus"),
                "192.168.1.10",
                502,
                true
        );

        assertThatThrownBy(() -> DeviceSnmpInstance.create(endpoint, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("endpoint protocol must be snmp");
    }

    @Test
    void create_withInstanceIdLessThanOne_throws() {
        assertThatThrownBy(() -> DeviceSnmpInstance.create(snmpEndpoint(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("instanceId must be greater than or equal to 1");
    }

    @Test
    void update_changesInstanceId() {
        DeviceSnmpInstance snmpInstance = DeviceSnmpInstance.create(snmpEndpoint(), 1);

        snmpInstance.update(11);

        assertThat(snmpInstance.getInstanceId()).isEqualTo(11);
    }

    private DeviceProtocolEndpoint snmpEndpoint() {
        return DeviceProtocolEndpoint.create(
                device(),
                protocolType("snmp", "SNMP"),
                "192.168.1.10",
                161,
                true
        );
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
