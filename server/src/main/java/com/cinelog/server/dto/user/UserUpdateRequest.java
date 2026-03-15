package com.cinelog.server.dto.user;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(min = 2, max = 10, message = "닉네임은 2~10자여야 합니다.")
    String userName
)
{}
