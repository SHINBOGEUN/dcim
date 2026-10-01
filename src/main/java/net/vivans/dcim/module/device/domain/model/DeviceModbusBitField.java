package net.vivans.dcim.module.device.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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

import java.util.Map;

@Entity
@Table(name = "device_modbus_bit_field")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceModbusBitField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reading_id", nullable = false)
    private DeviceModbusReading reading;

    @Column(name = "point_name", nullable = false)
    private String pointName;

    @Column(name = "bit_offset", nullable = false)
    private int bitOffset;

    @Column(name = "bit_width", nullable = false)
    private int bitWidth;

    @Convert(converter = ModbusBitValueMapConverter.class)
    @Column(name = "value_map", nullable = false, columnDefinition = "text")
    private Map<String, Long> valueMap;

    @Column(name = "unmapped_value")
    private Long unmappedValue;

    private DeviceModbusBitField(DeviceModbusReading reading, String pointName, int bitOffset,
                                 int bitWidth, Map<String, Long> valueMap, Long unmappedValue) {
        this.reading = reading;
        this.pointName = pointName;
        this.bitOffset = bitOffset;
        this.bitWidth = bitWidth;
        this.valueMap = Map.copyOf(valueMap == null ? Map.of() : valueMap);
        this.unmappedValue = unmappedValue;
    }

    public static DeviceModbusBitField create(DeviceModbusReading reading, String pointName, int bitOffset,
                                               int bitWidth, Map<String, Long> valueMap, Long unmappedValue) {
        return new DeviceModbusBitField(reading, pointName, bitOffset, bitWidth, valueMap, unmappedValue);
    }
}
