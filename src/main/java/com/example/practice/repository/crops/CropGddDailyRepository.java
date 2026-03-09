package com.example.practice.repository.crops;

import com.example.practice.entity.crops.CropGddDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface CropGddDailyRepository extends JpaRepository<CropGddDaily, Long> {

    Optional<CropGddDaily> findByCrops_CropsIdAndTargetDate(Long cropsId, LocalDate targetDate);

    List<CropGddDaily> findAllByCrops_CropsIdAndTargetDateBetweenOrderByTargetDateAsc(
            Long cropsId, LocalDate from, LocalDate to
    );

    @Query("""
        select c.targetDate
        from CropGddDaily c
        where c.crops.cropsId = :cropsId
          and c.targetDate between :from and :to
    """)
    List<LocalDate> findSavedDatesByCropsIdAndDateRange(@Param("cropsId") Long cropsId,
                                                         @Param("from") LocalDate from,
                                                         @Param("to") LocalDate to);

    @Query("""
        select (count(c) > 0)
        from CropGddDaily c
        where c.crops.cropsId = :cropsId
          and c.targetDate between :from and :to
          and c.source = :source
    """)
    boolean existsByCropsIdAndDateRangeAndSource(@Param("cropsId") Long cropsId,
                                                  @Param("from") LocalDate from,
                                                  @Param("to") LocalDate to,
                                                  @Param("source") String source);

    @Query("""
        select coalesce(sum(c.gdd), 0)
        from CropGddDaily c
        where c.crops.cropsId = :cropsId
          and c.targetDate between :from and :to
    """)
    BigDecimal sumGddByCropsIdAndDateRange(@Param("cropsId") Long cropsId,
                                           @Param("from") LocalDate from,
                                           @Param("to") LocalDate to);

    /**
     * Postgres UPSERT (crops_id + target_date 유니크 제약 필요)
     */
    @Modifying
    @Transactional
        @Query(value = """
        INSERT INTO crop_gdd_daily
        (crop_id, crops_id, target_date, gdd, gdd_normal_5y, base_temp,
         station_type, station_code, source, fetched_at, created_at, updated_at)
        VALUES
        (:cropId, :cropId, :targetDate, :gdd, :gddNormal5y, :baseTemp,
         :stationType, :stationCode, :source, :fetchedAt, now(), now())
        ON CONFLICT (crops_id, target_date)
        DO UPDATE SET
            crop_id = EXCLUDED.crop_id,
            gdd = EXCLUDED.gdd,
            gdd_normal_5y = EXCLUDED.gdd_normal_5y,
            base_temp = EXCLUDED.base_temp,
            station_type = EXCLUDED.station_type,
            station_code = EXCLUDED.station_code,
            source = EXCLUDED.source,
            fetched_at = EXCLUDED.fetched_at,
            updated_at = now()
        """, nativeQuery = true)
    void upsert(@Param("cropId") Long cropId,
                @Param("targetDate") LocalDate targetDate,
                @Param("gdd") BigDecimal gdd,
                @Param("gddNormal5y") BigDecimal gddNormal5y,
                @Param("baseTemp") BigDecimal baseTemp,
                @Param("stationType") String stationType,
                @Param("stationCode") String stationCode,
                @Param("source") String source,
                @Param("fetchedAt") OffsetDateTime fetchedAt);
}
