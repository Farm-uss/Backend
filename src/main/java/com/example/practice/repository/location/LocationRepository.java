package com.example.practice.repository.location;

import com.example.practice.entity.location.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    // 특정 농장의 위치 정보 조회 (1:1 관계일 때 유용)
    Optional<Location> findByFarmId(Long farmId);

    // 농장 삭제 시 위치 정보도 함께 삭제하기 위한 용도
    void deleteByFarmId(Long farmId);
}