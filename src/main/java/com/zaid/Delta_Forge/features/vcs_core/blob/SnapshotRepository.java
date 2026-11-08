package com.zaid.Delta_Forge.features.vcs_core.blob;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SnapshotRepository extends CrudRepository<Snapshot,SnapshotHash> {
    @Query("SELECT * FROM snapshots WHERE repository_id = :repoId AND hash = :hash")
    Optional<Snapshot> findBySnapshotHash(@Param("repoId") UUID repoId, @Param("hash") String hash);
}
