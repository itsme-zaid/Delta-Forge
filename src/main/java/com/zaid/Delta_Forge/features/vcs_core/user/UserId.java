package com.zaid.Delta_Forge.features.vcs_core.user;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID user_id) {
    public UserId{
        Objects.requireNonNull(user_id, "value");
    }

    public static UserId random() {
        return new UserId(UUID.randomUUID());
    }
}
