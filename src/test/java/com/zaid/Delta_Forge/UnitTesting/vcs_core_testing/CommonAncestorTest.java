package com.zaid.Delta_Forge.UnitTesting.vcs_core_testing;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;

import com.zaid.Delta_Forge.features.merge.MergeEngine;
import com.zaid.Delta_Forge.features.merge.MergeResult;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.commit.Commit;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepoService;
import com.zaid.Delta_Forge.features.vcs_core.repository.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.*;

public class CommonAncestorTest {
    JsonDocumentService jsonService;
    RepoService service;
    Repository repo;
    BranchName main = new BranchName("main");
    MergeEngine mergeEngine;

    @BeforeEach
    void setup() {
        jsonService = new JsonDocumentService(JsonMapper.builder().build());
        service = new RepoService(jsonService);
        mergeEngine = new MergeEngine(jsonService);
        repo = service.initializeRepository(
                "myrepo", "alice", "main",
                jsonService.parse("{\"a\": 0}"), "alice", "init");
    }

    private CommitId head(String branch) {
        return service.getBranch(repo.id(), new BranchName(branch)).headCommitId();
    }

    private Commit commitOn(String branch, CommitId expectedHead, int value, String msg) {
        return service.commit(repo.id(), new BranchName(branch), expectedHead,
                jsonService.parse("{\"a\": " + value + "}"), "alice", msg);
    }

//    private CommitId commitMerge(String branch, CommitId p1, CommitId p2, int value, String msg) {
//        return service.(repo.id(), new BranchName(branch),
//                p1, p2, jsonService.parse("{\"a\": " + value + "}"), "alice", msg).id();
//    }

    @Test
    void identical_heads_return_the_head_itself() {
        CommitId h = head("main");
        assertThat(service.findBestCommonAncestor(repo.id(), h, h)).isEqualTo(h);
    }

    @Test
    void identical_non_root_commits_return_themselves() {
        CommitId h0 = head("main");
        Commit c1 = commitOn("main", h0, 1, "c1");
        assertThat(service.findBestCommonAncestor(repo.id(), c1.id(), c1.id()))
                .isEqualTo(c1.id());
    }

    @Test
    void linear_history_base_is_the_ancestor() {
        // main: c0 → c1 → c2
        // Base of c2 and c0 is c0 (the ancestor wins).
        CommitId c0 = head("main");
        Commit c1 = commitOn("main", c0, 1, "c1");
        Commit c2 = commitOn("main", c1.id(), 2, "c2");

        assertThat(service.findBestCommonAncestor(repo.id(), c2.id(), c0)).isEqualTo(c0);
        assertThat(service.findBestCommonAncestor(repo.id(), c0, c2.id())).isEqualTo(c0);
        assertThat(service.findBestCommonAncestor(repo.id(), c2.id(), c1.id())).isEqualTo(c1.id());
    }

    @Test
    void simple_fork_finds_the_fork_point() {
        // c0 → c1 (main), c0 → c2 (feature)
        CommitId c0 = head("main");
        Commit c1 = commitOn("main", c0, 1, "c1");
        service.createBranch(repo.id(), new BranchName("feature/x"), c0);
        Commit c2 = commitOn("feature/x", c0, 2, "c2");

        assertThat(service.findBestCommonAncestor(repo.id(), c1.id(), c2.id()))
                .isEqualTo(c0);
    }

    @Test
    void asymmetric_depths_finds_the_correct_base() {
        // main:     c0 → c1 → c2 → c3 → c4 → c5 → c6 → c7  (ours head = c7)
        // feature:  branched at c2, then c8 → c9 → c10       (theirs head = c10)
        //
        // Base = c2. Reached by ours at level 5, by theirs at level 3.
        // (Or reversed — the point is the depths differ.)
        // If the BFS checks only frontiers, it misses c2 at the later level.
        CommitId c0 = head("main");
        Commit c1 = commitOn("main", c0, 1, "c1");
        Commit c2 = commitOn("main", c1.id(), 2, "c2");
        Commit c3 = commitOn("main", c2.id(), 3, "c3");
        Commit c4 = commitOn("main", c3.id(), 4, "c4");
        Commit c5 = commitOn("main", c4.id(), 5, "c5");
        Commit c6 = commitOn("main", c5.id(), 6, "c6");
        Commit c7 = commitOn("main", c6.id(), 7, "c7");

        service.createBranch(repo.id(), new BranchName("feature/x"), c2.id());
        Commit c8 = commitOn("feature/x", c2.id(), 8, "c8");
        Commit c9 = commitOn("feature/x", c8.id(), 9, "c9");
        Commit c10 = commitOn("feature/x", c9.id(), 10, "c10");

        assertThat(service.findBestCommonAncestor(repo.id(), c7.id(), c10.id()))
                .isEqualTo(c2.id());
    }

}
