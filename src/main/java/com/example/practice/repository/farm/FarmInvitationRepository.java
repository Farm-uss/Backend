package com.example.practice.repository.farm;

import com.example.practice.entity.farm.FarmInvitation;
import com.example.practice.entity.farm.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmInvitationRepository extends JpaRepository<FarmInvitation, Long> {

    // 내 초대 목록 (기존 메서드)
    List<FarmInvitation> findAllByInvitedUserIdAndStatus(Long userId, InvitationStatus status);

    Optional<FarmInvitation> findByFarmIdAndInvitedUserId(Long farmId, Long userId);

    boolean existsByFarmIdAndInvitedUserId(Long farmId, Long userId);

    void deleteAllByFarmId(Long farmId);
}
