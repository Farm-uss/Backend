package com.example.practice.repository.farm;

import com.example.practice.entity.farm.FarmMember;
import com.example.practice.entity.farm.FarmRole;
import com.example.practice.entity.user.User;
import com.example.practice.entity.location.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmMemberRepository extends JpaRepository<FarmMember, Long> {

    // ===== COUNT 쿼리들 =====
    long countByUserId(Long userId);
    long countByUserIdAndRole(Long userId, FarmRole role);

    // ===== 기본 CRUD =====
    Optional<FarmMember> findByFarmIdAndUserId(Long farmId, Long userId);
    void deleteAllByFarmId(Long farmId);
    boolean existsByFarmIdAndUserId(Long farmId, Long userId);

    // ===== 기존 핵심 메서드들 (서비스에서 사용 중) =====
    Optional<FarmMember> findByFarmIdAndRole(Long farmId, FarmRole role);
    long countByFarmIdAndRole(Long farmId, FarmRole role);
    List<FarmMember> findAllByFarmId(Long farmId);

    // ===== FETCH JOIN 최적화 (성능용) =====
    @Query("SELECT fm FROM FarmMember fm JOIN FETCH fm.farm WHERE fm.userId = :userId")
    List<FarmMember> findAllByMemberIdWithFarm(@Param("userId") Long userId);


    // 주의: fm.user FETCH는 FarmMember에 @ManyToOne User user 필드 필요!
    // 없으면 제거하거나 userRepo 별도 호출
}
