package net.vivans.dcim.module.externaldata.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import net.vivans.dcim.module.externaldata.domain.model.ExternalData;
import net.vivans.dcim.module.externaldata.infrastructure.persistence.ExternalDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExternalDataServiceTest {

    private ExternalDataRepository repository;
    private ExternalDataService service;

    @BeforeEach
    void setUp() {
        repository = mock(ExternalDataRepository.class);
        service = new ExternalDataService(repository, new ObjectMapper());
    }

    @Test
    void saveNormalizesCategoryAndPreservesArbitraryPayload() {
        when(repository.save(any(ExternalData.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var payload = new ObjectMapper().createObjectNode().put("resourceType", "GAS");
        payload.putObject("metrics").put("amount", 9876);

        var response = service.save(" billing ", "monthly", payload);

        assertThat(response.dataCategory()).isEqualTo("BILLING");
        assertThat(response.periodType()).isEqualTo("MONTHLY");
        assertThat(response.payload().path("resourceType").asText()).isEqualTo("GAS");
        assertThat(response.payload().path("metrics").path("amount").asInt()).isEqualTo(9876);
        assertThat(response.createDt()).isNotNull();
    }

    @Test
    void searchAppliesFiltersAndReturnsPagedResults() {
        Instant from = Instant.parse("2026-10-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-02T00:00:00Z");
        ExternalData row = ExternalData.receive("BILLING", "MONTHLY", "{}", from);
        when(repository.search(eq("BILLING"), eq("MONTHLY"), eq(from), eq(to), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(row)));

        var result = service.search("billing", "monthly", from, to, 1, 25);

        assertThat(result.content()).hasSize(1);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.content().get(0).payload().isObject()).isTrue();
        verify(repository).search(eq("BILLING"), eq("MONTHLY"), eq(from), eq(to),
                argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 25));
    }

    @Test
    void repeatedBusinessPayloadsAreSavedAsSeparateHistoryRows() {
        when(repository.save(any(ExternalData.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var payload = JsonNodeFactory.instance.objectNode().put("yearMonth", "2026-09");

        service.save("BILLING", "MONTHLY", payload);
        service.save("BILLING", "MONTHLY", payload);

        verify(repository, times(2)).save(any(ExternalData.class));
    }

    @Test
    void rejectsUnknownPeriodAndMalformedCategory() {
        var payload = JsonNodeFactory.instance.objectNode();

        assertThatThrownBy(() -> service.save("BILLING", "FORTNIGHTLY", payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unsupported periodType");
        assertThatThrownBy(() -> service.save("bad category", "DAILY", payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dataCategory");
    }

    @Test
    void rejectsMissingOrNonObjectPayloadAndInvalidPaging() {
        assertThatThrownBy(() -> service.save("BILLING", "MONTHLY", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("payload");
        assertThatThrownBy(() -> service.save("BILLING", "MONTHLY", JsonNodeFactory.instance.arrayNode()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JSON object");
        assertThatThrownBy(() -> service.search(null, null, null, null, 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.search(null, null, null, null, 1, 201))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
