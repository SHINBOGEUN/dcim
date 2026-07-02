package net.vivans.dcim.module.location.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationNodeCodeGeneratorTest {

    @Test
    void generate_createsTenCharacterBase62Code() {
        String code = LocationNodeCodeGenerator.generate();

        assertThat(code).hasSize(LocationNodeCodeGenerator.CODE_LENGTH);
        assertThat(code).matches("[0-9A-Za-z]{10}");
    }

    @Test
    void generate_createsUniqueCodes() {
        java.util.Set<String> codes = new java.util.HashSet<>();
        for (int i = 0; i < 100; i++) {
            codes.add(LocationNodeCodeGenerator.generate());
        }
        assertThat(codes).hasSize(100);
    }
}
