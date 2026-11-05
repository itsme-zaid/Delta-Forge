package com.zaid.Delta_Forge.features.vcs_core.repository;

import com.zaid.Delta_Forge.features.vcs_core.blob.Snapshot;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHash;
import com.zaid.Delta_Forge.features.vcs_core.branch.Branch;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.commit.Commit;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;

import java.util.*;

/**
 * In-memory state for one repository.
 * All state lives here; the service is stateless and operates on these.
 */
final class RepositoryState {
    final Repository repository;
    final Map<CommitId, Commit> commits = new HashMap<>();
    final Map<SnapshotHash, Snapshot> snapshots = new HashMap<>();
    final Map<BranchName, Branch> branches = new HashMap<>();

    RepositoryState(Repository repository) {
        this.repository = repository;
    }

    // Convenience accessors — fail loudly if referenced, missing
    Commit requireCommit(CommitId id) {
        Commit c = commits.get(id);
//        if (c == null) throw new NoSuchElementException("commit not found: " + id);
        return c;
    }

    Snapshot requireSnapshot(SnapshotHash h) {
        Snapshot s = snapshots.get(h);
        if (s == null) throw new NoSuchElementException("snapshot not found: " + h);
        return s;
    }

    Branch requireBranch(BranchName name) {
        Branch b = branches.get(name);
        if (b == null) throw new NoSuchElementException("branch not found: " + name);
        return b;
    }
}
