package com.example.practice.repository.user;

import com.example.practice.dto.user.UserSearchDto;
import com.example.practice.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // 대소문자 무시 LIKE '%nick%'
    List<User> findByNicknameContainingIgnoreCase(String nickname);

}
