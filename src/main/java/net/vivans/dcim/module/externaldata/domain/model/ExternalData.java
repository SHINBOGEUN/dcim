package net.vivans.dcim.module.externaldata.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "external_data")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_category", nullable = false, length = 50)
    private String dataCategory;

    @Column(name = "period_type", nullable = false, length = 30)
    private String periodType;

    @Column(name = "payload_json", nullable = false, columnDefinition = "JSON")
    private String payloadJson;

    @Column(name = "create_dt", nullable = false, updatable = false)
    private Instant createDt;

    private ExternalData(String dataCategory, String periodType, String payloadJson, Instant createDt) {
        this.dataCategory = dataCategory;
        this.periodType = periodType;
        this.payloadJson = payloadJson;
        this.createDt = createDt;
    }

    public static ExternalData receive(String dataCategory, String periodType, String payloadJson, Instant receivedAt) {
        return new ExternalData(dataCategory, periodType, payloadJson,
                receivedAt == null ? Instant.now() : receivedAt);
    }
}
