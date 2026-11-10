package com.zaid.Delta_Forge.features.vcs_core.repository;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Visibility {
    PUBLIC, PRIVATE;
    @JsonCreator
    public static Visibility fromString(String s) {
        return s == null ? PUBLIC : Visibility.valueOf(s.toUpperCase());
    }
}