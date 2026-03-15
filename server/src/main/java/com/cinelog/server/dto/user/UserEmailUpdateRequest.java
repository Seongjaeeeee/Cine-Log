package com.cinelog.server.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserEmailUpdateRequest(
    @NotNull
    @Email
    String email
)
{}
