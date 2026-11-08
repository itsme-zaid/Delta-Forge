package com.zaid.Delta_Forge.features.vcs_core.dto.auth;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record LoginResponse(
        UUID token,
        UUID userId,
        String username
) {}