package com.zaid.Delta_Forge.features.vcs_core.repository;


import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.vcs_core.blob.Snapshot;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHash;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHasher;
import com.zaid.Delta_Forge.features.vcs_core.branch.Branch;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.commit.Commit;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RepoService {

    private final JsonDocumentService jsonService;

    // In-memory store. Keyed by repository id.
    // ConcurrentHashMap so two concurrent initializeRepository calls can't collide;
    // the state inside each RepositoryState is not synchronized (single-threaded in v1).
    private final Map<RepositoryId, RepositoryState> stores = new ConcurrentHashMap<>();

    public RepoService(JsonDocumentService jsonService) {
        this.jsonService = jsonService;
    }

    // ============ initializeRepository ============

    public Repository initializeRepository(String name, String owner,
                                           String defaultBranchName,
                                           JsonNode initialDocument,
                                           String author, String message) {
        // 1. Create the Repository record.
        RepositoryId repoId = RepositoryId.random();
        Repository repo = new Repository(
                repoId, name, owner, defaultBranchName, Instant.now());

        // 2. Create the state container.
        RepositoryState state = new RepositoryState(repo);

        // 3. Store the initial snapshot + initial commit + initial branch,
        //    all in one logical step.

        SnapshotHash hash = null;
        try {
            hash = storeSnapshot(state, initialDocument);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        Commit rootCommit = createCommit(
                state, hash, List.of(), author, message);
        Branch defaultBranch = new Branch(
                UUID.randomUUID(), repoId,
                new BranchName(defaultBranchName),
                rootCommit.id(),
                Instant.now(), Instant.now());

        // 4. Register in state.
        state.branches.put(defaultBranch.name(), defaultBranch);
        stores.put(repoId, state);

        return repo;
    }

    // ============ createBranch ============

    public Branch createBranch(RepositoryId repoId,
                               BranchName newBranchName,
                               CommitId fromCommitId) {
        RepositoryState state = requireState(repoId);

        if (state.branches.containsKey(newBranchName)) {
            throw new IllegalStateException("branch already exists: " + newBranchName);
        }
        // Validate parent commit exists (this also validates fromCommitId is
        // in this repository — the FK invariant from Day 29).
        state.requireCommit(fromCommitId);

        Branch branch = new Branch(
                UUID.randomUUID(), repoId, newBranchName,
                fromCommitId, Instant.now(), Instant.now());
        state.branches.put(newBranchName, branch);
        return branch;
    }

    // ============ getBranch ============

    public Branch getBranch(RepositoryId repoId, BranchName name) {
        return requireState(repoId).requireBranch(name);
    }

    // ============ commit ============

    public Commit commit(RepositoryId repoId,
                         BranchName branchName,
                         CommitId expectedHead,
                         JsonNode newDocument,
                         String author,
                         String message) {
        RepositoryState state = requireState(repoId);

        // 1. Load the branch, verify the expected head matches (CAS — Day 32 refines this).
        Branch branch = state.requireBranch(branchName);
        if (!branch.headCommitId().equals(expectedHead)) {
            throw new RuntimeException(
                    "branch " + branchName + " moved from " + expectedHead
                            + " to " + branch.headCommitId());
        }

        // 2. Store the new snapshot (deduplicating by hash).
        SnapshotHash hash = null;
        try {
            hash = storeSnapshot(state, newDocument);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // 3. Create the commit.
        Commit newCommit = createCommit(
                state, hash, List.of(expectedHead), author, message);

        // 4. Update the branch head.
        Branch updated = new Branch(
                branch.id(), repoId, branchName,
                newCommit.id(), branch.createdAt(), Instant.now());
        state.branches.put(branchName, updated);

        return newCommit;
    }

    // ============ listCommits ============

    public List<Commit> listCommits(RepositoryId repoId, BranchName branchName) {
        RepositoryState state = requireState(repoId);
        Branch branch = state.requireBranch(branchName);
        CommitId head = branch.headCommitId();

        List<Commit> result = new ArrayList<>();
        Deque<CommitId> frontier = new ArrayDeque<>();
        Set<CommitId> visited = new HashSet<>();
        frontier.add(head);

        while (!frontier.isEmpty()) {
            CommitId id = frontier.poll();
            if (!visited.add(id)) continue;
            Commit c = state.requireCommit(id);
            result.add(c);
            // Enqueue parents (for a merge commit, this visits both branches of history)
            frontier.addAll(c.parents());
        }
        return result;
    }

    // ============ private helpers ============

    private RepositoryState requireState(RepositoryId repoId) {
        RepositoryState state = stores.get(repoId);
        if (state == null) throw new NoSuchElementException("repository not found: " + repoId);
        return state;
    }

    /**
     * Normalize + hash + store, deduplicating by hash.
     * Returns the hash. If a snapshot with the same hash already exists, returns
     * the existing one (no duplicate row).
     */
    private SnapshotHash storeSnapshot(RepositoryState state, JsonNode document) throws NoSuchAlgorithmException {
        // Day 15's normalization-v1: sort keys, normalize numbers.
        // For Day 30, we stub this — Day 15 work is when the canonical form lands.
        String canonical = jsonService.serialize(document);   // placeholder
        SnapshotHash hash = SnapshotHasher.hash(canonical);

        if (!state.snapshots.containsKey(hash)) {
            Snapshot snap = new Snapshot(hash, document, "normalization-v1", Instant.now());
            state.snapshots.put(hash, snap);
        }
        return hash;
    }

    private Commit createCommit(RepositoryState state,
                                SnapshotHash snapshotHash,
                                List<CommitId> parents,
                                String author,
                                String message) {
        // Validate parents exist in this repo
        for (CommitId p : parents) state.requireCommit(p);

        Commit commit = new Commit(
                CommitId.random(), state.repository.id(),
                snapshotHash, parents, author, message, Instant.now());
        state.commits.put(commit.id(), commit);
        return commit;
    }
}