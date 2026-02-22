package com.example.practice.repository.crops;

import com.example.practice.entity.crops.Crops;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropsRepository extends JpaRepository<Crops, Long> {

    // 특정 농장에서 재배 중인 모든 작물 리스트 조회
    List<Crops> findAllByFarmId(Long farmId);

    // 특정 작물 이름을 가진 농장들 찾기 (예: '감자'를 심은 농장 검색)
    List<Crops> findByNameContaining(String cropName);

    // 농장 삭제 시 등록된 작물 일괄 삭제
    void deleteAllByFarmId(Long farmId);
}