package com.zaid.Delta_Forge.features.vcs_core.branch;

import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import org.hibernate.validator.cfg.defs.UUIDDef;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends CrudRepository<Branch, UUID> {
    @Modifying
    @Query("""
    UPDATE branches
    SET head_commit_id = :newHead, updated_at = now()
    WHERE repository_id = :repoId
      AND branch_name = :branchName
      AND head_commit_id = :expectedHead
    """)
    int casHead(@Param("repoId") UUID repoId,
                @Param("branchName") String branchName,
                @Param("expectedHead") UUID expectedHead,
                @Param("newHead") UUID newHead);

    @Query("SELECT * FROM branches WHERE repository_id = :repoId AND branch_name = :name")
    Optional<Branch> findByRepositoryIdAndName(@Param("repoId") UUID repoId, @Param("name") String name);
    @Query("SELECT * FROM branches WHERE repository_id = :repoId")
    Optional<Branch> findByRepositoryId(@Param("repoId") UUID repoId);
}
