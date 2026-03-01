package com.example.practice.repository.crops;

import com.example.practice.entity.crops.CropGrowthStandard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CropGrowthStandardRepository extends JpaRepository<CropGrowthStandard, Long> {

    Optional<CropGrowthStandard> findByCropCode(String cropCode);

    Optional<CropGrowthStandard> findByCropName(String cropName);

    List<CropGrowthStandard> findAllByOrderByCropCodeAsc();
}

