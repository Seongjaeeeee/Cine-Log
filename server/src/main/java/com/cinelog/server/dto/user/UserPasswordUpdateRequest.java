package com.cinelog.server.dto.user;

import jakarta.validation.constraints.Size;

public record UserPasswordUpdateRequest(
    String password,
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
    String newPassword
){} 
