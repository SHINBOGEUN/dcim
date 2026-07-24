package net.vivans.dcim.module.devicemodel.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.vivans.dcim.shared.persistence.BaseEntity;

@Entity
@Table(name = "device_model_modbus_point")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceModelModbusPoint extends BaseEntity {

    private static final String MODBUS_PROTOCOL_CODE = "modbus";
    private static final int ADDRESS_MIN = 0;
    private static final int ADDRESS_MAX = 65535;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_protocol_id", nullable = false)
    private DeviceModelProtocol modelProtocol;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "register_type", nullable = false, length = 30)
    private ModbusRegisterType registerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private ModbusDataType dataType;

    @Enumerated(EnumType.STRING)
    @Column(name = "byte_order", length = 10)
    private ModbusByteOrder byteOrder;

    // 고정 주소면 값 보유, 인스턴스가 주소를 주면 null (requiresInstance=true)
    private Integer address;

    // true = 주소를 인스턴스(endpoint_modbus)가 제공 (분전반 회로 등)
    @Column(name = "requires_instance", nullable = false)
    private boolean requiresInstance;

    private Double scale;

    @Column(length = 50)
    private String unit;

    @Column(nullable = false)
    private boolean enabled;


    private DeviceModelModbusPoint(
            DeviceModelProtocol modelProtocol,
            String name,
            ModbusRegisterType registerType,
            ModbusDataType dataType,
            ModbusByteOrder byteOrder,
            Integer address,
            boolean requiresInstance,
            Double scale,
            String unit,
            boolean enabled
    ) {
        validateModelProtocol(modelProtocol);
        validateName(name);
        validateRegisterType(registerType);
        validateDataType(dataType);
        validateByteOrder(dataType, byteOrder);
        validateAddress(requiresInstance, address);
        this.modelProtocol = modelProtocol;
        this.name = name;
        this.registerType = registerType;
        this.dataType = dataType;
        this.byteOrder = byteOrder;
        this.address = address;
        this.requiresInstance = requiresInstance;
        this.scale = scale;
        this.unit = unit;
        this.enabled = enabled;
    }

    public static DeviceModelModbusPoint create(
            DeviceModelProtocol modelProtocol,
            String name,
            ModbusRegisterType registerType,
            ModbusDataType dataType,
            ModbusByteOrder byteOrder,
            Integer address,
            boolean requiresInstance,
            Double scale,
            String unit,
            boolean enabled
    ) {
        return new DeviceModelModbusPoint(
                modelProtocol, name, registerType, dataType, byteOrder,
                address, requiresInstance, scale, unit, enabled
        );
    }

    public void update(
            String name,
            ModbusRegisterType registerType,
            ModbusDataType dataType,
            ModbusByteOrder byteOrder,
            Integer address,
            boolean requiresInstance,
            Double scale,
            String unit,
            boolean enabled
    ) {
        validateName(name);
        validateRegisterType(registerType);
        validateDataType(dataType);
        validateByteOrder(dataType, byteOrder);
        validateAddress(requiresInstance, address);
        this.name = name;
        this.registerType = registerType;
        this.dataType = dataType;
        this.byteOrder = byteOrder;
        this.address = address;
        this.requiresInstance = requiresInstance;
        this.scale = scale;
        this.unit = unit;
        this.enabled = enabled;
    }


    private static void validateModelProtocol(DeviceModelProtocol modelProtocol) {
        if (modelProtocol == null) {
            throw new IllegalArgumentException("modelProtocol is required");
        }
        if (!MODBUS_PROTOCOL_CODE.equals(modelProtocol.getProtocolType().getCode())) {
            throw new IllegalArgumentException("protocol must be modbus");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
    }

    private static void validateRegisterType(ModbusRegisterType registerType) {
        if (registerType == null) {
            throw new IllegalArgumentException("registerType is required");
        }
    }

    private static void validateDataType(ModbusDataType dataType) {
        if (dataType == null) {
            throw new IllegalArgumentException("dataType is required");
        }
    }

    private static void validateByteOrder(ModbusDataType dataType, ModbusByteOrder byteOrder) {
        if (dataType.isMultiRegister() && byteOrder == null) {
            throw new IllegalArgumentException("byteOrder is required for multi-register data type");
        }
        if (!dataType.isMultiRegister() && byteOrder != null) {
            throw new IllegalArgumentException("byteOrder must be null for single-register data type");
        }
    }

    private static void validateAddress(boolean requiresInstance, Integer address) {
        if (requiresInstance && address != null) {
            throw new IllegalArgumentException("address must be null when requiresInstance is true");
        }
        if (!requiresInstance && address == null) {
            throw new IllegalArgumentException("address is required when requiresInstance is false");
        }
        if (address != null && (address < ADDRESS_MIN || address > ADDRESS_MAX)) {
            throw new IllegalArgumentException("address must be between 0 and 65535");
        }
    }
}

