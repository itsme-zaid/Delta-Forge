package com.zaid.Delta_Forge.features.vcs_core.user;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.OffsetDateTime;

@Table("users")
public record User(
        @Id UserId id,
        @Column("username") String username,
        @Column("password_hash") String passwordHash,
        @Column("created_at") OffsetDateTime createdAt
) {}