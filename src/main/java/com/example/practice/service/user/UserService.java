package com.example.practice.service.user;

import com.example.practice.dto.user.UserSearchDto;
import com.example.practice.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;

    public List<UserSearchDto> searchUsers(String nickname) {
        if (nickname == null || nickname.trim().length() < 1) {
            return List.of();
        }
        return userRepo.findByNicknameContainingIgnoreCase(nickname.trim())
                .stream()
                .map(user -> new UserSearchDto(user.getId(), user.getNickname()))
                .toList();
    }
}

