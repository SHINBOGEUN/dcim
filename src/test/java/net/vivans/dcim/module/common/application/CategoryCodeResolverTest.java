package net.vivans.dcim.module.common.application;

import jakarta.persistence.EntityNotFoundException;
import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.common.domain.repository.CommonCodeRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryCodeResolverTest {
    private final CommonCodeRepository repository = mock(CommonCodeRepository.class);
    private final CategoryCodeResolver resolver = new CategoryCodeResolver(repository);

    @Test
    void resolve_whenIdIsNull_returnsNull() {
        assertThat(resolver.resolve(null)).isNull();
    }

    @Test
    void resolve_whenCodeBelongsToCategory_returnsCode() {
        CommonCode category = code("CATEGORY", "POWER");
        when(repository.findById(13)).thenReturn(Optional.of(category));

        assertThat(resolver.resolve(13)).isSameAs(category);
    }

    @Test
    void resolve_whenCodeBelongsToAnotherGroup_throws() {
        when(repository.findById(53)).thenReturn(Optional.of(code("UNIT", "W")));

        assertThatThrownBy(() -> resolver.resolve(53))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("categoryCodeId must belong to CATEGORY group");
    }

    @Test
    void resolve_whenCodeDoesNotExist_throws() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolve(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("CommonCode not found: 999");
    }

    private CommonCode code(String groupKey, String code) {
        CodeGroup group = CodeGroup.createCodeGroup(groupKey, groupKey);
        return CommonCode.createCommonCode(group, code, code, 1);
    }
}
