package com.example.practice.repository.crops;

import com.example.practice.entity.crops.DiseaseInfo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiseaseInfoRepository extends JpaRepository<DiseaseInfo, String> {

    @EntityGraph(attributePaths = "guides")
    Optional<DiseaseInfo> findByDiseaseIdAndActiveTrue(String diseaseId);
}
