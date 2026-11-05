package com.zaid.Delta_Forge.UnitTesting.vcs_core_testing;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHasher;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepoService;
import com.zaid.Delta_Forge.features.vcs_core.repository.Repository;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.zaid.Delta_Forge.features.json.ValueSlot.jsonService;
import static org.assertj.core.api.Assertions.assertThat;

public class RepoServiceTest {
    private JsonDocumentService jsonDocumentService;

    @BeforeEach
    public void setUp() {
        jsonDocumentService = new JsonDocumentService(JsonMapper.builder().build());
    }

    private Arbitrary<ObjectNode> wrapInObject(Arbitrary<ObjectNode> inner) {
        return inner.map(child -> {
            ObjectNode obj = JsonNodeFactory.instance.objectNode();
            obj.set("k", child);
            return obj;
        });
    }

    @Provide
    Arbitrary<ObjectNode> documents() {
        return Arbitraries.recursive(
                () -> Arbitraries.just(JsonNodeFactory.instance.objectNode()),  // base: {}
                this::wrapInObject,
                1, 5
        );
    }

    @Test
    void same_document_different_key_order_same_hash() {
        JsonNode a = jsonDocumentService.parse("{\"x\": 1, \"y\": 2}");
        JsonNode b = jsonDocumentService.parse("{\"y\": 2, \"x\": 1}");

        String canonicalA = jsonDocumentService.serialize(a);
        String canonicalB = jsonDocumentService.serialize(b);

        assertThat(canonicalA).isEqualTo(canonicalB);
        assertThat(SnapshotHasher.hash(canonicalA))
                .isEqualTo(SnapshotHasher.hash(canonicalB));
    }

    //    @Test
//    void concurrent_commits_to_same_branch_exactly_one_succeeds() throws Exception {
//        RepoService service = new RepoService(jsonService);
//        Repository repo = service.initializeRepository(
//                "myrepo", "alice", "main",
//                jsonService.parse("{\"a\": 0}"), "alice", "init");
//        CommitId baseHead = service.getBranch(repo.id(), new BranchName("main")).headCommitId();
//
//        int threads = 10;
//        ExecutorService pool = Executors.newFixedThreadPool(threads);
//        CountDownLatch start = new CountDownLatch(1);
//        CountDownLatch done = new CountDownLatch(threads);
//
//        AtomicInteger successes = new AtomicInteger();
//        AtomicInteger staleFailures = new AtomicInteger();
//        List<Future<?>> futures = new ArrayList<>();
//
//        for (int i = 0; i < threads; i++) {
//            final int idx = i;
//            futures.add(pool.submit(() -> {
//                try {
//                    start.await();
//                    service.commit(
//                            repo.id(), new BranchName("main"), baseHead,
//                            jsonService.parse("{\"a\": " + idx + "}"),
//                            "alice", "concurrent " + idx);
//                    successes.incrementAndGet();
//                } catch (IllegalStateException e) {
//                    staleFailures.incrementAndGet();
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                } finally {
//                    done.countDown();
//                }
//            }));
//        }
//
//        start.countDown();          // release all threads at once
//        done.await(10, TimeUnit.SECONDS);
//        pool.shutdown();
//
//        // Exactly one commit succeeds; the rest see STALE_HEAD.
//        assertThat(successes.get()).isEqualTo(1);
//        assertThat(staleFailures.get()).isEqualTo(threads - 1);
//
//        // The branch head is the winner's commit, not lost.
//        CommitId newHead = service.getBranch(repo.id(), new BranchName("main")).headCommitId();
//        assertThat(newHead).isNotEqualTo(baseHead);
//
//        // History has exactly 2 commits: the initial + the winner.
//        assertThat(service.listCommits(repo.id(), new BranchName("main")))
//                .hasSize(2);
//    }
    @Test
    public void list_commits_print() {

    }
}
