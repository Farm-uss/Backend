package com.example.practice.repository.crops;

import com.example.practice.entity.crops.Crops;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropsRepository extends JpaRepository<Crops, Long> {

    // 특정 농장에서 재배 중인 모든 작물 리스트 조회
    List<Crops> findAllByFarm_Id(Long farmId);

    // 특정 작물 이름을 가진 농장들 찾기 (예: '감자'를 심은 농장 검색)
    List<Crops> findByNameContaining(String cropName);

    List<Crops> findAllByPlantingDateIsNotNull();

    // 농장 삭제 시 등록된 작물 일괄 삭제
    void deleteAllByFarm_Id(Long farmId);

    @Query("""
        select c
        from Crops c
        join fetch c.farm f
        join fetch c.growthStandard gs
        where c.cropsId = :id
    """)
    Optional<Crops> findByIdWithFarm(@Param("id") Long id);

    @Query("""
        select c
        from Crops c
        join fetch c.farm f
        left join fetch c.growthStandard gs
        where c.cropsId = :cropsId
          and f.id = :farmId
    """)
    Optional<Crops> findByFarmIdAndCropsIdWithGrowthStandard(@Param("farmId") Long farmId,
                                                              @Param("cropsId") Long cropsId);
    @Query("""
        select c
        from Crops c
        join fetch c.farm f
        left join fetch c.growthStandard gs
        where f.id = :farmId
        order by c.plantingDate desc
    """)
    List<Crops> findCurrentCropCandidatesByFarmId(@Param("farmId") Long farmId);
}
