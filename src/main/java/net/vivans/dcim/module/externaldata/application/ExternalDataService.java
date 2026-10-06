package net.vivans.dcim.module.externaldata.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.externaldata.api.dto.ExternalDataResponse;
import net.vivans.dcim.module.externaldata.domain.model.ExternalData;
import net.vivans.dcim.module.externaldata.infrastructure.persistence.ExternalDataRepository;
import net.vivans.dcim.shared.api.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class ExternalDataService {

    private static final Set<String> PERIOD_TYPES = Set.of(
            "REALTIME", "MINUTELY", "HOURLY", "DAILY", "WEEKLY", "MONTHLY", "YEARLY");
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("[A-Z][A-Z0-9_-]{0,49}");
    private static final int MAX_PAGE_SIZE = 200;

    private final ExternalDataRepository repository;
    private final ObjectMapper objectMapper;

    public ExternalDataResponse save(String category, String periodType, JsonNode payload) {
        String normalizedCategory = normalizeCategory(category);
        String normalizedPeriod = normalizePeriod(periodType);
        validatePayload(payload);
        try {
            ExternalData saved = repository.save(ExternalData.receive(
                    normalizedCategory, normalizedPeriod, objectMapper.writeValueAsString(payload), Instant.now()));
            return toResponse(saved);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("payload could not be serialized as JSON", exception);
        }
    }

    @Transactional(readOnly = true)
    public ExternalDataResponse get(long id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("external data not found: " + id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExternalDataResponse> search(String category, String periodType,
                                                Instant receivedFrom, Instant receivedTo,
                                                int page, int size) {
        String normalizedCategory = category == null || category.isBlank() ? null : normalizeCategory(category);
        String normalizedPeriod = periodType == null || periodType.isBlank() ? null : normalizePeriod(periodType);
        if (page < 1) {
            throw new IllegalArgumentException("page must be 1 or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (receivedFrom != null && receivedTo != null && !receivedFrom.isBefore(receivedTo)) {
            throw new IllegalArgumentException("receivedTo must be later than receivedFrom");
        }

        Page<ExternalData> result = repository.search(normalizedCategory, normalizedPeriod, receivedFrom, receivedTo,
                PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createDt"), Sort.Order.desc("id"))));
        return PageResponse.from(result, this::toResponse);
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("dataCategory is required");
        }
        String normalized = category.trim().toUpperCase(Locale.ROOT);
        if (!CATEGORY_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("dataCategory has an invalid format");
        }
        return normalized;
    }

    private String normalizePeriod(String periodType) {
        if (periodType == null || periodType.isBlank()) {
            throw new IllegalArgumentException("periodType is required");
        }
        String normalized = periodType.trim().toUpperCase(Locale.ROOT);
        if (!PERIOD_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("unsupported periodType: " + normalized);
        }
        return normalized;
    }

    private static void validatePayload(JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            throw new IllegalArgumentException("payload must be a JSON object");
        }
    }

    private ExternalDataResponse toResponse(ExternalData entity) {
        try {
            return new ExternalDataResponse(entity.getId(), entity.getDataCategory(), entity.getPeriodType(),
                    objectMapper.readTree(entity.getPayloadJson()), entity.getCreateDt());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("stored external data payload is not valid JSON: id=" + entity.getId(), exception);
        }
    }
}
