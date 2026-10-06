package net.vivans.dcim.module.externaldata.infrastructure.persistence;

import net.vivans.dcim.module.externaldata.domain.model.ExternalData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ExternalDataRepository extends JpaRepository<ExternalData, Long> {

    @Query("""
            select data from ExternalData data
            where (:category is null or data.dataCategory = :category)
              and (:period is null or data.periodType = :period)
              and (:receivedFrom is null or data.createDt >= :receivedFrom)
              and (:receivedTo is null or data.createDt < :receivedTo)
            """)
    Page<ExternalData> search(
            @Param("category") String category,
            @Param("period") String period,
            @Param("receivedFrom") Instant receivedFrom,
            @Param("receivedTo") Instant receivedTo,
            Pageable pageable);

    List<ExternalData> findByDataCategoryAndPeriodTypeOrderByCreateDtDescIdDesc(String dataCategory, String periodType);
}
