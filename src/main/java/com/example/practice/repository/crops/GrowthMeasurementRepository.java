package com.example.practice.repository.crops;

import com.example.practice.entity.crops.GrowthMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface GrowthMeasurementRepository extends JpaRepository<GrowthMeasurement, Long> {

    List<GrowthMeasurement> findAllByCrops_CropsIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
            Long cropsId,
            OffsetDateTime from,
            OffsetDateTime to
    );

    Optional<GrowthMeasurement> findTopByCrops_CropsIdAndMeasuredAtLessThanOrderByMeasuredAtDesc(
            Long cropsId,
            OffsetDateTime measuredAt
    );

    Optional<GrowthMeasurement> findTopByCrops_CropsIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(
            Long cropsId,
            OffsetDateTime from,
            OffsetDateTime to
    );
}
