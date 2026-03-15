package com.cinelog.server.dto.user;

import java.time.LocalDateTime;

import com.cinelog.server.domain.Role;
import com.cinelog.server.domain.User;

public record UserResponse(
    Long id,
    String userName,
    String email,
    LocalDateTime createdAt,
    Role role
){
    public static UserResponse of(User user){
        return new UserResponse(user.getId(),user.getName(),user.getEmail(),user.getCreatedAt(),user.getRole());
    }
}
