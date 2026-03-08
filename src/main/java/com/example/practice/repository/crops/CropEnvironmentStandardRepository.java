package com.example.practice.repository.crops;

import com.example.practice.entity.environment.CropEnvironmentStandard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CropEnvironmentStandardRepository extends JpaRepository<CropEnvironmentStandard, Long> {

    Optional<CropEnvironmentStandard> findByCropCode(String cropCode);
}