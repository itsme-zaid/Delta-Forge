package com.zaid.Delta_Forge.features.vcs_core.ai.validation;

import com.zaid.Delta_Forge.features.merge.MergeConflict;

import java.util.Objects;

public final class ConflictIds {
    public static String of(MergeConflict c) {
        return "c-" + Integer.toHexString(
                Objects.hash(c.path().toString(), c.conflictType()));
    }
}