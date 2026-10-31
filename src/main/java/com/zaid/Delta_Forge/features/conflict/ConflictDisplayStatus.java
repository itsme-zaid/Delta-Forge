package com.zaid.Delta_Forge.features.conflict;

public enum ConflictDisplayStatus {
    UNRESOLVED,       // no Resolution
    AI_SUGGESTED,     // AI proposal exists but not yet resolved (Day 49)
    USER_ACCEPTED,    // Resolution with source AI_ACCEPTED or AI_EDITED
    MANUALLY_RESOLVED // Resolution with source MANUAL
}
