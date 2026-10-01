package net.vivans.dcim.module.device.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter
public class ModbusBitValueMapConverter implements AttributeConverter<Map<String, Long>, String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Long>> TYPE = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(Map<String, Long> values) {
        try {
            return MAPPER.writeValueAsString(values == null ? Map.of() : values);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("invalid bit value map", exception);
        }
    }

    @Override
    public Map<String, Long> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return MAPPER.readValue(json, TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("invalid stored bit value map", exception);
        }
    }
}
