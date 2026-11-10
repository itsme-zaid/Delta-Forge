package com.zaid.Delta_Forge.features.vcs_core.merge;

import com.zaid.Delta_Forge.features.merge.MergeConflict;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public sealed interface MergeReport permits MergeReport.Merged, MergeReport.Conflicted {

    /**
     * The VCS-level outcome of a merge.
     * <p>
     * Two variants:
     * - Merged: the merge had no conflicts, a merge commit was created, and
     * the target branch head moved. Carries the commit id and the merged doc.
     * - Conflicted: the merge has conflicts; nothing was committed, the target
     * branch head is unchanged. Carries the partial tree and the conflict list.
     * <p>
     * This is the type the REST API returns (200 for Merged, 409 for Conflicted).
     * The merge engine itself returns MergeResult; MergeReport is one layer above.
     */

    record Merged(CommitId mergeCommitId, JsonNode mergedDocument) implements MergeReport {
    }

    record Conflicted(UUID mergeRequestId, JsonNode partialTree, List<MergeConflict> conflicts) implements MergeReport {
        public Conflicted {
            Objects.requireNonNull(mergeRequestId, "mergeRequestId");
            conflicts = List.copyOf(conflicts);
        }
    }
}
