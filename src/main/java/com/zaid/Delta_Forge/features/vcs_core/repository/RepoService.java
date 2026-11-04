package com.zaid.Delta_Forge.features.vcs_core.repository;


import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.vcs_core.blob.Snapshot;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHash;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHasher;
import com.zaid.Delta_Forge.features.vcs_core.branch.Branch;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.commit.Commit;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitSummary;
import com.zaid.Delta_Forge.features.vcs_core.exceptions.UnrelatedHistoriesException;
import com.zaid.Delta_Forge.features.vcs_core.exceptions.UnsupportedHistoryException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        } catch (Exception e) {
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
        synchronized (state){
            if (!branch.headCommitId().equals(expectedHead)) {
                throw new RuntimeException(
                        "branch " + branchName + " moved from " + expectedHead
                                + " to " + branch.headCommitId());

            }
        }
        // 2. Store the new snapshot (deduplicating by hash).
        SnapshotHash hash = null;
        try {
            hash = storeSnapshot(state, newDocument);
        } catch (Exception e) {
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

    // CommitSummary DTO — new file

    public List<CommitSummary> listCommits(RepositoryId repoId,
                                           BranchName branchName,
                                           int limit,
                                           int offset) {
        RepositoryState state = requireState(repoId);
        Branch branch = state.requireBranch(branchName);
        CommitId head = branch.headCommitId();

        // 1. BFS from head (reachable set)
        List<Commit> reachable = bfsFromHead(state, head);

        // 2. Sort: newest first, then by id for determinism
        reachable.sort(Comparator
                .comparing(Commit::createdAt).reversed()
                .thenComparing(commit -> commit.id().value()));

        // 3. Slice for pagination
        List<Commit> page = reachable.stream()
                .skip(offset)
                .limit(limit)
                .toList();

        // 4. Compute branch labels for the page's commits
        Map<CommitId, List<String>> branchLabels = branchesByCommit(state);

        // 5. Convert to CommitSummary
        return page.stream()
                .map(c -> new CommitSummary(
                        c.id(), c.author(), c.message(), c.createdAt(),
                        c.snapshotHash(), c.parents(),
                        branchLabels.getOrDefault(c.id(), List.of())))
                .toList();
    }

    public CommitSummary getCommit(RepositoryId repoId, CommitId commitId) {
        RepositoryState state = requireState(repoId);
        Commit c = state.requireCommit(commitId);
        Map<CommitId, List<String>> branchLabels = branchesByCommit(state);
        return new CommitSummary(
                c.id(), c.author(), c.message(), c.createdAt(),
                c.snapshotHash(), c.parents(),
                branchLabels.getOrDefault(c.id(), List.of()));
    }

    public CommitId findRoot(RepositoryId repoId, BranchName branchName) {
        RepositoryState state = requireState(repoId);
        CommitId current = state.requireBranch(branchName).headCommitId();
        while (true) {
            Commit c = state.requireCommit(current);
            if (c.isRootCommit()) return current;
            current = c.parents().get(0);
        }
    }

// --- private helpers ---

    private List<Commit> bfsFromHead(RepositoryState state, CommitId head) {
        List<Commit> result = new ArrayList<>();
        Deque<CommitId> frontier = new ArrayDeque<>();
        Set<CommitId> visited = new HashSet<>();
        frontier.add(head);
        while (!frontier.isEmpty()) {
            CommitId id = frontier.poll();
            if (!visited.add(id)) continue;
            Commit c = state.requireCommit(id);
            result.add(c);
            frontier.addAll(c.parents());
        }
        return result;
    }

    private Set<CommitId> reachableFrom(RepositoryState state, CommitId head) {
        return bfsFromHead(state, head).stream()
                .map(Commit::id)
                .collect(Collectors.toSet());
    }

    private Map<CommitId, List<String>> branchesByCommit(RepositoryState state) {
        Map<CommitId, List<String>> result = new HashMap<>();
        for (Branch b : state.branches.values()) {
            for (CommitId id : reachableFrom(state, b.headCommitId())) {
                result.computeIfAbsent(id, k -> new ArrayList<>()).add(b.name().value());
            }
        }
        result.values().forEach(list -> list.sort(String::compareTo));
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
    private SnapshotHash storeSnapshot(RepositoryState state, JsonNode document){
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
    public CommitId findBestCommonAncestor(RepositoryId repoId, CommitId ours, CommitId theirs) {
        List<CommitId> candidates = common(repoId, ours, theirs);
        if (candidates.isEmpty()) {
            throw new UnrelatedHistoriesException(ours, theirs);
        }
        if (candidates.size() > 1) {
            throw new UnsupportedHistoryException(
                    "criss-cross merge: multiple best common ancestors " + candidates);
        }
        return candidates.getFirst();
    }
    private List<CommitId> common(RepositoryId repoId, CommitId ours, CommitId theirs) {
        RepositoryState state = stores.get(repoId);
        if (ours.equals(theirs)) return new ArrayList<>(List.of(ours));

        Set<CommitId> oursVisited   = new HashSet<>();
        Set<CommitId> theirsVisited = new HashSet<>();
        Deque<CommitId> oursFrontier   = new ArrayDeque<>();
        Deque<CommitId> theirsFrontier = new ArrayDeque<>();

        oursVisited.add(ours);
        theirsVisited.add(theirs);
        oursFrontier.offer(ours);
        theirsFrontier.offer(theirs);

        List<CommitId> ans = new ArrayList<>();

        while (!oursFrontier.isEmpty() || !theirsFrontier.isEmpty()) {
            int oursSize   = oursFrontier.size();
            int theirsSize = theirsFrontier.size();

            // Expand the full current level on the ours side.
            for (int i = 0; i < oursSize; i++) {
                Commit x = state.requireCommit(oursFrontier.poll());
                for (CommitId p : x.parents()) {
                    if (oursVisited.add(p)) {
                        oursFrontier.offer(p);
                        if (theirsVisited.contains(p)) ans.add(p);
                    }
                }
            }

            // Expand the full current level on the theirs side.
            for (int i = 0; i < theirsSize; i++) {
                Commit x = state.requireCommit(theirsFrontier.poll());
                for (CommitId p : x.parents()) {
                    if (theirsVisited.add(p)) {
                        theirsFrontier.offer(p);
                        if (oursVisited.contains(p)) ans.add(p);
                    }
                }
            }

            if (!ans.isEmpty()) return ans;
        }
        return ans;   // empty → UnrelatedHistoriesException at the caller
    }
    public Commit createMergeCommit(RepositoryId repoId, BranchName branch,
                                    CommitId parent1, CommitId parent2,
                                    JsonNode document, String author, String message) {
        RepositoryState state = requireState(repoId);
        synchronized (state) {
            Branch b = state.requireBranch(branch);
            state.requireCommit(parent1);
            state.requireCommit(parent2);

            SnapshotHash hash = storeSnapshot(state, document);
            Commit mergeCommit = createCommit(
                    state, hash, List.of(parent1, parent2), author, message);
            Branch updated = new Branch(
                    b.id(), repoId, branch,
                    mergeCommit.id(), b.createdAt(), Instant.now());
            state.branches.put(branch, updated);
            return mergeCommit;
        }
    }

//    CommitId createOrphanCommitForTest(RepositoryId repoId, JsonNode document,
//                                       String author, String message) {
//        RepositoryState state = requireState(repoId);
//        synchronized (state) {
//            SnapshotHash hash = storeSnapshot(state, document);
//            Commit c = createCommit(state, hash, List.of(), author, message);
//            return c.id();
//        }
//    }

}