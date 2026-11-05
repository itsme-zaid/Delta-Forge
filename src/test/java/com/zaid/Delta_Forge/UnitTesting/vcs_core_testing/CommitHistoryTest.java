package com.zaid.Delta_Forge.UnitTesting.vcs_core_testing;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.vcs_core.branch.Branch;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.commit.Commit;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitSummary;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepoService;
import com.zaid.Delta_Forge.features.vcs_core.repository.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CommitHistoryTest {
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
                jsonService.parse("{\"a\": 1}"), "alice", "init");
    }

    @Test
    void list_commits_returns_newest_first() {
        CommitId h0 = service.getBranch(repo.id(), main).headCommitId();
        Commit c1 = service.commit(repo.id(), main, h0,
                jsonService.parse("{\"a\": 2}"), "alice", "1");
        Commit c2 = service.commit(repo.id(), main, c1.id(),
                jsonService.parse("{\"a\": 3}"), "alice", "2");

        List<CommitSummary> history = service.listCommits(repo.id(), main, 100, 0);
        assertThat(history).extracting(CommitSummary::id)
                .containsExactly(c2.id(), c1.id(), h0);
    }

    @Test
    void commit_summary_includes_branch_labels() {
        CommitId h0 = service.getBranch(repo.id(), main).headCommitId();
        Commit c1 = service.commit(repo.id(), main, h0,
                jsonService.parse("{\"a\": 2}"), "alice", "1");
        // Create a branch at c1, so both main and feature point there (via ancestry)
        service.createBranch(repo.id(), new BranchName("feature/x"), c1.id());

        List<CommitSummary> history = service.listCommits(repo.id(), main, 100, 0);
        // c1 is the head of feature/x and is contained in main
        CommitSummary c1Summary = history.stream()
                .filter(s -> s.id().equals(c1.id()))
                .findFirst().orElseThrow();
        assertThat(c1Summary.branches())
                .containsExactly("feature/x", "main");   // sorted alphabetically
    }

    @Test
    void list_commits_paginates_stably() {
        CommitId head = service.getBranch(repo.id(), main).headCommitId();
        for (int i = 0; i < 5; i++) {
            head = service.commit(repo.id(), main, head,
                    jsonService.parse("{\"a\": " + i + "}"), "alice", "c" + i).id();
        }

        List<CommitSummary> page1 = service.listCommits(repo.id(), main, 2, 0);
        List<CommitSummary> page2 = service.listCommits(repo.id(), main, 2, 2);

        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(2);
        // No overlap
        assertThat(page1).extracting(CommitSummary::id)
                .doesNotContainAnyElementsOf(page2.stream().map(CommitSummary::id).toList());
    }

    @Test
    void get_commit_returns_full_summary() {
        Commit c = service.commit(repo.id(), main,
                service.getBranch(repo.id(), main).headCommitId(),
                jsonService.parse("{\"a\": 2}"), "alice", "the message");

        CommitSummary summary = service.getCommit(repo.id(), c.id());
        assertThat(summary.message()).isEqualTo("the message");
        assertThat(summary.author()).isEqualTo("alice");
        assertThat(summary.parentIds()).hasSize(1);
    }
}
