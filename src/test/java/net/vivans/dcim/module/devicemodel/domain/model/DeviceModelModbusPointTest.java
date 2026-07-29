package net.vivans.dcim.module.devicemodel.domain.model;

import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeviceModelModbusPointTest {

    @Test
    void create_withFixedAddress_succeeds() {
        DeviceModelModbusPoint point = DeviceModelModbusPoint.create(
                modbusProtocol(),
                "OFF-TEMP",
                ModbusRegisterType.INPUT,
                ModbusDataType.INT16,
                null,
                257,
                false,
                0.1,
                "°C",
                true
        );

        assertThat(point.getName()).isEqualTo("OFF-TEMP");
        assertThat(point.getRegisterType()).isEqualTo(ModbusRegisterType.INPUT);
        assertThat(point.getDataType()).isEqualTo(ModbusDataType.INT16);
        assertThat(point.getByteOrder()).isNull();
        assertThat(point.getAddress()).isEqualTo(257);
        assertThat(point.isRequiresInstance()).isFalse();
        assertThat(point.getScale()).isEqualTo(0.1);
        assertThat(point.getUnit()).isEqualTo("°C");
        assertThat(point.isEnabled()).isTrue();
    }

    @Test
    void create_withInstanceAddress_succeeds() {
        DeviceModelModbusPoint point = DeviceModelModbusPoint.create(
                modbusProtocol(),
                "TOTAL_WT",
                ModbusRegisterType.HOLDING,
                ModbusDataType.FLOAT32,
                ModbusByteOrder.CDAB,
                null,
                true,
                1000.0,
                "W",
                true
        );

        assertThat(point.isRequiresInstance()).isTrue();
        assertThat(point.getAddress()).isNull();
        assertThat(point.getByteOrder()).isEqualTo(ModbusByteOrder.CDAB);
    }

    @Test
    void create_withoutName_throws() {
        assertThatThrownBy(() -> DeviceModelModbusPoint.create(
                modbusProtocol(), " ",
                ModbusRegisterType.INPUT, ModbusDataType.INT16, null,
                257, false, 0.1, "°C", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name is required");
    }

    @Test
    void create_withoutRegisterType_throws() {
        assertThatThrownBy(() -> DeviceModelModbusPoint.create(
                modbusProtocol(), "OFF-TEMP",
                null, ModbusDataType.INT16, null,
                257, false, 0.1, "°C", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("registerType is required");
    }

    @Test
    void create_withoutDataType_throws() {
        assertThatThrownBy(() -> DeviceModelModbusPoint.create(
                modbusProtocol(), "OFF-TEMP",
                ModbusRegisterType.INPUT, null, null,
                257, false, 0.1, "°C", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("dataType is required");
    }

    @Test
    void create_multiRegisterWithoutByteOrder_throws() {
        assertThatThrownBy(() -> DeviceModelModbusPoint.create(
                modbusProtocol(), "TOTAL_WT",
                ModbusRegisterType.HOLDING, ModbusDataType.FLOAT32, null,
                null, true, 1000.0, "W", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("byteOrder is required for multi-register data type");
    }

    @Test
    void create_singleRegisterWithByteOrder_throws() {
        assertThatThrownBy(() -> DeviceModelModbusPoint.create(
                modbusProtocol(), "OFF-TEMP",
                ModbusRegisterType.INPUT, ModbusDataType.INT16, ModbusByteOrder.CDAB,
                257, false, 0.1, "°C", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("byteOrder must be null for single-register data type");
    }

    @Test
    void create_requiresInstanceWithAddress_throws() {
        assertThatThrownBy(() -> DeviceModelModbusPoint.create(
                modbusProtocol(), "TOTAL_WT",
                ModbusRegisterType.HOLDING, ModbusDataType.FLOAT32, ModbusByteOrder.CDAB,
                11667, true, 1000.0, "W", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("address must be null when requiresInstance is true");
    }

    @Test
    void create_fixedAddressWithoutAddress_throws() {
        assertThatThrownBy(() -> DeviceModelModbusPoint.create(
                modbusProtocol(), "OFF-TEMP",
                ModbusRegisterType.INPUT, ModbusDataType.INT16, null,
                null, false, 0.1, "°C", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("address is required when requiresInstance is false");
    }

    @Test
    void create_withAddressOutOfRange_throws() {
        assertThatThrownBy(() -> DeviceModelModbusPoint.create(
                modbusProtocol(), "OFF-TEMP",
                ModbusRegisterType.INPUT, ModbusDataType.INT16, null,
                70000, false, 0.1, "°C", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("address must be between 0 and 65535");
    }

    @Test
    void create_withNonModbusProtocol_throws() {
        DeviceModel model = DeviceModel.create("PDU-3P", "Vendor", modelType(), null);
        CommonCode snmp = protocolType("snmp", "SNMP");
        DeviceModelProtocol protocol = DeviceModelProtocol.of(model, snmp);

        assertThatThrownBy(() -> DeviceModelModbusPoint.create(
                protocol, "V",
                ModbusRegisterType.HOLDING, ModbusDataType.INT16, null,
                1, false, null, null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("protocol must be modbus");
    }

    @Test
    void update_replacesFields() {
        DeviceModelModbusPoint point = DeviceModelModbusPoint.create(
                modbusProtocol(), "OFF-TEMP",
                ModbusRegisterType.INPUT, ModbusDataType.INT16, null,
                257, false, 0.1, "°C", true);

        point.update(
                "TOTAL_WT",
                ModbusRegisterType.HOLDING, ModbusDataType.FLOAT32, ModbusByteOrder.CDAB,
                null, true, 1000.0, "W", false);

        assertThat(point.getName()).isEqualTo("TOTAL_WT");
        assertThat(point.getDataType()).isEqualTo(ModbusDataType.FLOAT32);
        assertThat(point.getByteOrder()).isEqualTo(ModbusByteOrder.CDAB);
        assertThat(point.getAddress()).isNull();
        assertThat(point.isRequiresInstance()).isTrue();
        assertThat(point.isEnabled()).isFalse();
    }

    private DeviceModelProtocol modbusProtocol() {
        DeviceModel model = DeviceModel.create("RDC", "USystems", modelType(), null);
        return DeviceModelProtocol.of(model, protocolType("modbus", "Modbus"));
    }

    private CommonCode modelType() {
        CodeGroup group = CodeGroup.createCodeGroup("MODEL_TYPE", "Model Type");
        return CommonCode.createCommonCode(group, "RDC", "RDC", 1);
    }

    private CommonCode protocolType(String code, String name) {
        CodeGroup group = CodeGroup.createCodeGroup("PROTOCOL_TYPE", "Protocol Type");
        return CommonCode.createCommonCode(group, code, name, 1);
    }
}
