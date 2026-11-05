package com.zaid.Delta_Forge.UnitTesting.vcs_core_testing;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.merge.MergeResult;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.commit.Commit;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitSummary;
import com.zaid.Delta_Forge.features.vcs_core.exceptions.StaleHeadException;
import com.zaid.Delta_Forge.features.vcs_core.merge.MergeReport;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepoService;
import com.zaid.Delta_Forge.features.vcs_core.repository.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MergeIntegratedTest {
    JsonDocumentService jsonService;
    RepoService service;
    Repository repo;
    BranchName main = new BranchName("main");

    @BeforeEach
    void setup() {
        jsonService = new JsonDocumentService(JsonMapper.builder().build());
        service = new RepoService(jsonService);
        repo = service.initializeRepository(
                "myrepo", "alice", "main",
                jsonService.parse("{\"a\": 1, \"b\": 1}"), "alice", "init");
    }

    private CommitId head(String branch) {
        return service.getBranch(repo.id(), new BranchName(branch)).headCommitId();
    }

    private Commit commitOn(String branch, CommitId expected, String json, String msg) {
        return service.commit(repo.id(), new BranchName(branch), expected,
                jsonService.parse(json), "alice", msg);
    }

    // ============================================================
    // No conflict → mergeAndReport commits, returns Merged
    // ============================================================

    @Test
    void no_conflict_merge_commits_and_returns_Merged() {
        // main:     c0 → c1 (a=10)
        // feature:  c0 → c2 (b=20)
        CommitId c0 = head("main");
        Commit c1 = commitOn("main", c0, "{\"a\": 10, \"b\": 1}", "c1");
        service.createBranch(repo.id(), new BranchName("feature/x"), c0);
        Commit c2 = commitOn("feature/x", c0, "{\"a\": 1, \"b\": 20}", "c2");

        CommitId expected = head("main");
        MergeReport report = service.mergeAndReport(
                repo.id(), new BranchName("feature/x"), main,
                expected, "alice", "merge feature/x into main");

        // Result is Merged
        assertThat(report).isInstanceOf(MergeReport.Merged.class);
        MergeReport.Merged m = (MergeReport.Merged) report;
        assertThat(m.mergeCommitId()).isNotNull();
        assertThat(m.mergedDocument().get("a").asInt()).isEqualTo(10);
        assertThat(m.mergedDocument().get("b").asInt()).isEqualTo(20);

        // main's head moved to the merge commit
        assertThat(head("main")).isEqualTo(m.mergeCommitId());

        // The merge commit has two parents
        Commit mergeCommit = service.getCommitRecord(repo.id(), m.mergeCommitId());
        assertThat(mergeCommit.parents()).hasSize(2);
        assertThat(mergeCommit.parents().get(0)).isEqualTo(c1.id());   // target's previous head
        assertThat(mergeCommit.parents().get(1)).isEqualTo(c2.id());   // source's head
    }

    // ============================================================
    // Conflict → mergeAndReport returns Conflicted, no commit
    // ============================================================

    @Test
    void conflict_merge_returns_Conflicted_and_does_not_move_target() {
        // main:     c0 → c1 (a=10)
        // feature:  c0 → c2 (a=20)
        // conflicting edits to /a
        CommitId c0 = head("main");
        Commit c1 = commitOn("main", c0, "{\"a\": 10, \"b\": 1}", "c1");
        service.createBranch(repo.id(), new BranchName("feature/x"), c0);
        Commit c2 = commitOn("feature/x", c0, "{\"a\": 20, \"b\": 1}", "c2");

        CommitId targetBefore = head("main");
        MergeReport report = service.mergeAndReport(
                repo.id(), new BranchName("feature/x"), main,
                targetBefore, "alice", "merge feature/x into main");

        assertThat(report).isInstanceOf(MergeReport.Conflicted.class);
        MergeReport.Conflicted c = (MergeReport.Conflicted) report;
        assertThat(c.conflicts()).hasSize(1);
        assertThat(c.conflicts().getFirst().path().toString()).isEqualTo("/a");

        // CRITICAL: target head unchanged, no commit created
        assertThat(head("main")).isEqualTo(targetBefore);

        // History unchanged: main still has c0 → c1
        assertThat(service.listCommits(repo.id(), main, 100, 0)).hasSize(2);
    }

    // ============================================================
    // Stale target head → StaleHeadException on apply
    // ============================================================

    @Test
    void stale_target_head_during_apply_throws_StaleHead() {
        CommitId c0 = head("main");
        commitOn("main", c0, "{\"a\": 10, \"b\": 1}", "c1");
        service.createBranch(repo.id(), new BranchName("feature/x"), c0);
        commitOn("feature/x", c0, "{\"a\": 1, \"b\": 20}", "c2");

        // Compute a merge against current head
        MergeResult r = service.merge(repo.id(), new BranchName("feature/x"), main);
        assertThat(r).isInstanceOf(MergeResult.MergeSuccess.class);
        JsonNode merged = ((ValueSlot.Present)
                ((MergeResult.MergeSuccess) r).mergedOutcome()).node();
        CommitId expected = head("main");

        // Simulate concurrent commit on main (moves the head)
        commitOn("main", expected, "{\"a\": 99, \"b\": 1}", "concurrent");

        // Now apply with the stale expected head
        assertThatThrownBy(() -> service.applyMerge(
                repo.id(), new BranchName("feature/x"), main,
                expected, merged, "alice", "merge"))
                .isInstanceOf(StaleHeadException.class);
    }

    // ============================================================
    // merge() is pure — no commits, no branch moves
    // ============================================================

    @Test
    void merge_is_pure_and_has_no_side_effects() {
        CommitId c0 = head("main");
        commitOn("main", c0, "{\"a\": 10, \"b\": 1}", "c1");
        service.createBranch(repo.id(), new BranchName("feature/x"), c0);
        commitOn("feature/x", c0, "{\"a\": 1, \"b\": 20}", "c2");

        CommitId targetBefore = head("main");
        List<CommitSummary> historyBefore = service.listCommits(repo.id(), main, 100, 0);

        // Call merge() — should not touch anything
        MergeResult r = service.merge(repo.id(), new BranchName("feature/x"), main);
        assertThat(r).isInstanceOf(MergeResult.MergeSuccess.class);

        // Target head unchanged, history unchanged
        assertThat(head("main")).isEqualTo(targetBefore);
        assertThat(service.listCommits(repo.id(), main, 100, 0)).isEqualTo(historyBefore);
    }

}
