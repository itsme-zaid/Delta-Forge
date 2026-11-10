package com.zaid.Delta_Forge.features.vcs_core.blob;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SnapshotRepository extends CrudRepository<Snapshot,SnapshotHash> {
    @Query("SELECT * FROM snapshots WHERE repository_id = :repoId AND hash = :hash")
    Optional<Snapshot> findByRepoIdAndHash(@Param("repoId") UUID repoId, @Param("hash") String hash);

    @Modifying
    @Query("""
    INSERT INTO snapshots (repository_id, hash, document, canonical_bytes, normalization_version, created_at)
    VALUES (:repoId, :hash, :document::jsonb, :bytes, :version, :createdAt)
    """)
    void insertSnapshot(@Param("repoId") UUID repoId, @Param("hash") String hash,
                        @Param("document") String documentJson, @Param("bytes") byte[] bytes,
                        @Param("version") String version, @Param("createdAt") Instant createdAt);
}
