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
}
